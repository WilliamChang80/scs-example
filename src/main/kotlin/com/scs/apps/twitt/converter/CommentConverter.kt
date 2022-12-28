package com.scs.apps.twitt.converter

import com.scs.apps.twitt.EnrichedCommentMessage
import com.scs.apps.twitt.entity.Author
import com.scs.apps.twitt.entity.Comment
import com.scs.apps.twitt.utils.DateTimeUtils
import org.springframework.stereotype.Component
import java.util.*

@Component
class CommentConverter(private val dateTimeUtils: DateTimeUtils) {

    fun convertToEntity(commentMessage: EnrichedCommentMessage): Comment {
        val user = Author(name = commentMessage.user.name, profilePic = commentMessage.user.profilePic)
        user.id = UUID.fromString(commentMessage.user.id)

        val comment = Comment(comment = commentMessage.comment, postId = commentMessage.postId, user = user)
        comment.createdAt = dateTimeUtils.parseToZdt(commentMessage.createdAt)
        comment.updatedAt = dateTimeUtils.parseToZdt(commentMessage.updatedAt)
        comment.id = UUID.fromString(commentMessage.id)

        return comment
    }
}