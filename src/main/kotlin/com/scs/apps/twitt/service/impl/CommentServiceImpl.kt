package com.scs.apps.twitt.service.impl

import com.scs.apps.twitt.Comment.CommentKey
import com.scs.apps.twitt.Comment.CommentMessage
import com.scs.apps.twitt.constant.KafkaTopic
import com.scs.apps.twitt.dto.RequestDto
import com.scs.apps.twitt.producer.StreamsProducer
import com.scs.apps.twitt.serde.CommentSerde
import com.scs.apps.twitt.service.CommentService
import com.scs.apps.twitt.utils.DateTimeUtils
import com.scs.apps.twitt.utils.UuidUtils
import org.apache.kafka.streams.KeyValue
import org.springframework.stereotype.Service

@Service
class CommentServiceImpl(
    private val streamsProducer: StreamsProducer,
    private val commentSerde: CommentSerde,
    private val uuidUtils: UuidUtils,
    private val dateTimeUtils: DateTimeUtils
) : CommentService {

    override fun createComment(createCommentRequestDto: RequestDto.CreateCommentRequestDto, userId: String) {
        val uuid: String = uuidUtils.randomUuid().toString()
        val createdAt = dateTimeUtils.now()

        val commentKey: CommentKey = CommentKey.newBuilder()
            .setId(uuid)
            .build()

        val (comment, postId) = createCommentRequestDto
        val commentMessage: CommentMessage = CommentMessage.newBuilder()
            .setId(uuid)
            .setUserId(userId)
            .setComment(comment)
            .setPostId(postId)
            .setCreatedAt(createdAt)
            .setUpdatedAt(createdAt)
            .build()

        streamsProducer.publish(
            KafkaTopic.COMMENT_CREATED_TOPIC, KeyValue.pair(commentKey, commentMessage),
            commentSerde.commentKeySerde(), commentSerde.commentMessageSerde()
        )
    }
}