package com.scs.apps.twitt.function

import com.scs.apps.twitt.Comment.CommentKey
import com.scs.apps.twitt.Comment.CommentMessage
import com.scs.apps.twitt.EnrichedCommentKey
import com.scs.apps.twitt.EnrichedCommentMessage
import com.scs.apps.twitt.UserKey
import com.scs.apps.twitt.UserMessage
import com.scs.apps.twitt.constant.CommentKTable
import com.scs.apps.twitt.constant.EnrichedCommentKStream
import com.scs.apps.twitt.constant.MaterializedConstant
import com.scs.apps.twitt.constant.UserKTable
import com.scs.apps.twitt.joiner.CommentJoiner
import com.scs.apps.twitt.serde.CommentSerde
import com.scs.apps.twitt.serde.EnrichedCommentSerde
import lombok.extern.slf4j.Slf4j
import org.apache.kafka.streams.kstream.Materialized
import org.apache.kafka.streams.kstream.Named
import org.apache.kafka.streams.kstream.TableJoined
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.function.BiFunction
import java.util.function.Function

@Configuration
@Slf4j
class CommentJoinerFunction(
    private val commentSerde: CommentSerde, private val enrichedCommentSerde: EnrichedCommentSerde,
    private val commentJoiner: CommentJoiner
) {

    @Bean
    fun commentUserJoiner(): BiFunction<CommentKTable, UserKTable, EnrichedCommentKStream> {
        return BiFunction<CommentKTable, UserKTable, EnrichedCommentKStream> { commentTable, userTable ->
            return@BiFunction commentTable.leftJoin(
                userTable,
                extractCommentForeignKey(),
                { comment: CommentMessage, user: UserMessage? -> commentJoiner.joinCommentWithUser(comment, user) },
                TableJoined.`as`(MaterializedConstant.COMMENT_USER_JOIN),
                Materialized.with(commentSerde.commentKeySerde(), enrichedCommentSerde.enrichedCommentMessageSerde())
            )
                .toStream(Named.`as`(MaterializedConstant.COMMENT_USER_JOINED))
                .selectKey(
                    { key: CommentKey, g: EnrichedCommentMessage? -> selectEnrichedCommentKey(key, g) },
                    Named.`as`(MaterializedConstant.COMMENT_USER_JOIN_STREAM_REKEY_ID)
                )
                .peek { key: EnrichedCommentKey, value: EnrichedCommentMessage? ->
                    println("joined EnrichedCommentMessage with key $key and value $value")
                }
        }
    }

    private fun selectEnrichedCommentKey(key: CommentKey, g: EnrichedCommentMessage?): EnrichedCommentKey {
        return EnrichedCommentKey.newBuilder().setId(key.id).build()
    }

    private fun extractCommentForeignKey(): Function<CommentMessage, UserKey> {
        return Function { comment: CommentMessage -> UserKey.newBuilder().setId(comment.userId).build() }
    }

}