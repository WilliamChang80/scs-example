package com.scs.apps.twitt.function

import com.scs.apps.twitt.*
import com.scs.apps.twitt.Comment.CommentKey
import com.scs.apps.twitt.Comment.CommentMessage
import com.scs.apps.twitt.constant.CommentKTable
import com.scs.apps.twitt.constant.EnrichedCommentKStream
import com.scs.apps.twitt.constant.MaterializedConstant
import com.scs.apps.twitt.constant.UserKTable
import com.scs.apps.twitt.joiner.CommentJoiner
import com.scs.apps.twitt.serde.CommentSerde
import com.scs.apps.twitt.serde.EnrichedCommentSerde
import com.scs.apps.twitt.serde.PostSerde
import mu.KotlinLogging
import org.apache.kafka.common.utils.Bytes
import org.apache.kafka.streams.kstream.*
import org.apache.kafka.streams.state.KeyValueStore
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.function.BiFunction
import java.util.function.Consumer
import java.util.function.Function

@Configuration
class CommentJoinerFunction(
    private val commentSerde: CommentSerde, private val enrichedCommentSerde: EnrichedCommentSerde,
    private val commentJoiner: CommentJoiner, private val postSerde: PostSerde
) {

    private val logger = KotlinLogging.logger {}

    @Bean
    fun commentUserJoiner(): BiFunction<CommentKTable, UserKTable, EnrichedCommentKStream> {
        return BiFunction<CommentKTable, UserKTable, EnrichedCommentKStream> { commentTable, userTable ->
            return@BiFunction commentTable.join(
                userTable,
                extractCommentForeignKey(),
                { comment: CommentMessage, user: UserMessage? -> commentJoiner.joinCommentWithUser(comment, user) },
                TableJoined.`as`(MaterializedConstant.COMMENT_USER_JOIN),
                Materialized.with(commentSerde.commentKeySerde(), enrichedCommentSerde.enrichedCommentMessageSerde())
            )
                .toStream(Named.`as`(MaterializedConstant.COMMENT_USER_JOINED))
                .selectKey(
                    { key: CommentKey, _ -> selectEnrichedCommentKey(key) },
                    Named.`as`(MaterializedConstant.COMMENT_USER_JOIN_STREAM_REKEY_ID)
                )
                .peek { key: EnrichedCommentKey, value: EnrichedCommentMessage? ->
                    logger.info("joined EnrichedCommentMessage with key $key and value $value")
                }
        }
    }

    @Bean
    fun updatePostCommentCount(): Consumer<EnrichedCommentKStream> {
        return Consumer { comment ->
            comment.filter({ _, value -> value.id != "" }, Named.`as`("filter-comment-with-empty-id"))
                .groupBy(
                    { _, value ->
                        PostKey.newBuilder().setId(value.postId).build()
                    }, Grouped.with(postSerde.postKeySerde(), enrichedCommentSerde.enrichedCommentMessageSerde())
                        .withName("comments-group-by-post-id")
                ).count()
                .toStream()
                .peek { k, v -> logger.info("post with id ${k.id} have $v comments") }
        }
    }

    private fun selectEnrichedCommentKey(key: CommentKey): EnrichedCommentKey {
        return EnrichedCommentKey.newBuilder().setId(key.id).build()
    }

    private fun extractCommentForeignKey(): Function<CommentMessage, UserKey> {
        return Function { comment: CommentMessage -> UserKey.newBuilder().setId(comment.userId).build() }
    }

}