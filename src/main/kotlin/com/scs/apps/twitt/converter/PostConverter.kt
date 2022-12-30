package com.scs.apps.twitt.converter

import com.google.protobuf.Timestamp
import com.scs.apps.twitt.EnrichedPostMessage
import com.scs.apps.twitt.PostCdcKey
import com.scs.apps.twitt.PostCdcMessage
import com.scs.apps.twitt.entity.Author
import com.scs.apps.twitt.entity.Post
import com.scs.apps.twitt.utils.DateTimeUtils
import org.apache.kafka.streams.KeyValue
import org.springframework.stereotype.Component
import java.util.*
import java.util.stream.Collectors

@Component
class PostConverter(private val commentConverter: CommentConverter, private val dateTimeUtils: DateTimeUtils) {

    fun toEntity(enrichedPostMessage: EnrichedPostMessage): Post {
        val user = Author(name = enrichedPostMessage.user.name, profilePic = enrichedPostMessage.user.profilePic)
        user.id = UUID.fromString(enrichedPostMessage.user.id)

        val comments = enrichedPostMessage.commentsList.stream()
            .map { comment -> commentConverter.convertToEntity(comment) }.collect(Collectors.toList())

        val post = Post(
            title = enrichedPostMessage.title, content = enrichedPostMessage.content,
            isDeleted = enrichedPostMessage.isDeleted, rating = enrichedPostMessage.rating.toDouble(),
            creator = user, comments = comments, dislikes = enrichedPostMessage.dislikes,
            likes = enrichedPostMessage.likes, views = enrichedPostMessage.views
        )
        post.id = UUID.fromString(enrichedPostMessage.id)
        post.createdAt = dateTimeUtils.parseToZdt(enrichedPostMessage.createdAt)
        post.updatedAt = dateTimeUtils.parseToZdt(enrichedPostMessage.updatedAt)

        return post
    }

    fun toPostCdcMessage(post: Post): KeyValue<PostCdcKey, PostCdcMessage> {
        val messageKey: PostCdcKey = PostCdcKey.newBuilder()
            .setId(post.id.toString())
            .build()

        val createdAt: Timestamp? = post.createdAt?.let { dateTimeUtils.parseToTimestamp(it) }
        val message: PostCdcMessage = PostCdcMessage.newBuilder()
            .setId(post.id.toString())
            .setCreatorId(post.creator?.id.toString())
            .setCreatedAt(createdAt)
            .setUpdatedAt(createdAt)
            .setContent(post.content)
            .setTitle(post.title)
            .setIsDeleted(false)
            .build()

        return KeyValue.pair(messageKey, message)
    }
}