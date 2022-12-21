package com.scs.apps.twitt.joiner

import com.scs.apps.twitt.Comment.CommentMessage
import com.scs.apps.twitt.EnrichedCommentMessage
import com.scs.apps.twitt.UserMessage
import org.apache.kafka.streams.kstream.ValueJoiner
import org.springframework.stereotype.Component

@Component
class CommentJoiner {
    fun joinCommentWithUser(comment: CommentMessage, user: UserMessage?): EnrichedCommentMessage {
        println("joining commentMessage $comment with user message $user")
        val enrichedCommentBuilder: EnrichedCommentMessage.Builder =
            EnrichedCommentMessage.newBuilder().setComment(comment.comment).setId(comment.id)
                .setCreatedAt(comment.createdAt).setUpdatedAt(comment.updatedAt)

        if (user != null) {
            enrichedCommentBuilder.userId = user.id
            enrichedCommentBuilder.user = user
        }

        return enrichedCommentBuilder.build()
    }
}