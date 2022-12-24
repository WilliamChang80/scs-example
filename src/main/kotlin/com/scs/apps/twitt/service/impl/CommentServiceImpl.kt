package com.scs.apps.twitt.service.impl

import com.google.protobuf.Timestamp
import com.scs.apps.twitt.Comment.CommentKey
import com.scs.apps.twitt.Comment.CommentMessage
import com.scs.apps.twitt.constant.KafkaTopic
import com.scs.apps.twitt.dto.RequestDto
import com.scs.apps.twitt.producer.StreamsProducer
import com.scs.apps.twitt.serde.CommentSerde
import com.scs.apps.twitt.service.CommentService
import org.apache.kafka.streams.KeyValue
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.*

@Service
class CommentServiceImpl(
    private val streamsProducer: StreamsProducer,
    private val commentSerde: CommentSerde
) : CommentService {

    override fun createComment(createCommentRequestDto: RequestDto.CreateCommentRequestDto, userId: String) {
        val uuid: String = UUID.randomUUID().toString()
        val now = Instant.now()
        val createdAt = Timestamp.newBuilder().setSeconds(now.epochSecond).setNanos(now.nano).build()

        val commentKey: CommentKey = CommentKey.newBuilder()
            .setId(uuid)
            .build()

        val (comment, postId) = createCommentRequestDto
        val commentMessage: CommentMessage = CommentMessage.newBuilder()
            .setId(userId)
            .setUserId(userId)
            .setComment(comment)
            .setPostId(postId)
            .setUpdatedAt(createdAt)
            .build()

        streamsProducer.publish(
            KafkaTopic.COMMENT_CREATED_TOPIC, KeyValue.pair(commentKey, commentMessage),
            commentSerde.commentKeySerde(), commentSerde.commentMessageSerde()
        )
    }
}