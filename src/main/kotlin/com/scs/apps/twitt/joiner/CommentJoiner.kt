package com.scs.apps.twitt.joiner

import com.scs.apps.twitt.Comment.CommentMessage
import com.scs.apps.twitt.EnrichedCommentMessage
import com.scs.apps.twitt.UserMessage
import mu.KotlinLogging
import org.springframework.stereotype.Component

@Component
class CommentJoiner {
    private val logger = KotlinLogging.logger {}

    fun joinCommentWithUser(comment: CommentMessage, user: UserMessage?): EnrichedCommentMessage {
        logger.info("joining commentMessage $comment with user message $user")
        val enrichedCommentBuilder: EnrichedCommentMessage.Builder =
            EnrichedCommentMessage.newBuilder().setComment(comment.comment).setId(comment.id).setPostId(comment.postId)
                .setCreatedAt(comment.createdAt).setUpdatedAt(comment.updatedAt)

        if (user != null) {
            enrichedCommentBuilder.userId = user.id
            enrichedCommentBuilder.user = user
        }

        return enrichedCommentBuilder.build()
    }
}