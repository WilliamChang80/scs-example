package com.scs.apps.twitt.function

import com.scs.apps.twitt.*
import com.scs.apps.twitt.constant.*
import com.scs.apps.twitt.group.ActivityGroup
import com.scs.apps.twitt.group.CommentGroup
import com.scs.apps.twitt.group.PostGroup
import com.scs.apps.twitt.joiner.PostJoiner
import com.scs.apps.twitt.serde.EnrichedPostSerde
import mu.KotlinLogging
import org.apache.kafka.common.utils.Bytes
import org.apache.kafka.streams.kstream.Materialized
import org.apache.kafka.streams.kstream.Named
import org.apache.kafka.streams.kstream.TableJoined
import org.apache.kafka.streams.state.KeyValueStore
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.function.BiFunction
import java.util.function.Function

@Configuration
class PostJoinerFunction(
    private val enrichedPostSerde: EnrichedPostSerde, private val commentGroup: CommentGroup,
    private val postGroup: PostGroup, private val postJoiner: PostJoiner, private val activityGroup: ActivityGroup
) {

    private val logger = KotlinLogging.logger {}

    @Bean
    fun joinPost(): Function<EnrichedCommentKStream, Function<AggregatedActivityKStream, Function<EnrichedPostKStream, EnrichedPostKStream>>> {
        return Function { enrichedComment: EnrichedCommentKStream ->
            return@Function Function activity@{ aggregatedActivity: AggregatedActivityKStream ->
                return@activity Function { enrichedPost: EnrichedPostKStream ->
                    enrichedPost.groupByKey()
                        .cogroup { key: EnrichedPostKey, value: EnrichedPostMessage, result: EnrichedPostMessage ->
                            postGroup.cogroupAggregator(key, value, result)
                        }
                        .cogroup(
                            commentGroup.group(enrichedComment)
                        ) { key: EnrichedPostKey, value: EnrichedCommentMessage, result: EnrichedPostMessage ->
                            commentGroup.cogroupAggregator(
                                key,
                                value,
                                result
                            )
                        }
                        .cogroup(
                            activityGroup.group(aggregatedActivity)
                        ) { key: EnrichedPostKey, value: AggregatedActivityMessage, result: EnrichedPostMessage ->
                            activityGroup.cogroupAggregator(
                                key,
                                value,
                                result
                            )
                        }
                        .aggregate({ initializeEnrichedPostMessage() }, materializedSerde())
                        .toStream()
                        .peek { key, value -> logger.info("joined enriched post with key $key and value $value") }
                }
            }
        }
    }

    @Bean
    fun joinPostWithUser(): BiFunction<PostKTable, UserKTable, EnrichedPostKStream> {
        return BiFunction { postTable: PostKTable, userTable: UserKTable ->
            postTable.join(
                userTable,
                { post: PostMessage -> postJoiner.foreignKeyExtractor(post) },
                { post: PostMessage, user: UserMessage -> postJoiner.joinPostWithUser(post, user) },
                TableJoined.`as`(MaterializedConstant.POST_USER_JOIN),
                postJoiner.materialized()
            )
                .toStream(Named.`as`("post-user-joined"))
                .selectKey { _, value: EnrichedPostMessage -> EnrichedPostKey.newBuilder().setId(value.id).build() }
                .peek { k: EnrichedPostKey, v: EnrichedPostMessage ->
                    logger.info("joined post with user with key: $$k and value $v")
                }
        }
    }

    private fun initializeEnrichedPostMessage(): EnrichedPostMessage {
        return EnrichedPostMessage.newBuilder().build()
    }

    private fun materializedSerde(): Materialized<EnrichedPostKey, EnrichedPostMessage, KeyValueStore<Bytes, ByteArray>> {
        return Materialized.`as`<EnrichedPostKey, EnrichedPostMessage, KeyValueStore<Bytes, ByteArray>>("enriched-post-state-store")
            .withKeySerde(enrichedPostSerde.enrichedPostKeySerde())
            .withValueSerde(enrichedPostSerde.enrichedPostMessageSerde())
    }

}
