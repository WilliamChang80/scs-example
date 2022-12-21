package com.scs.apps.twitt.service.impl

import com.google.protobuf.Timestamp
import com.scs.apps.twitt.PostCdcKey
import com.scs.apps.twitt.PostCdcMessage
import com.scs.apps.twitt.constant.KafkaTopic
import com.scs.apps.twitt.dto.CreatePostRequestDto
import com.scs.apps.twitt.producer.StreamsProducer
import com.scs.apps.twitt.serde.ProtobufSerde
import com.scs.apps.twitt.service.PostService
import org.apache.kafka.streams.KeyValue
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.*


@Service
class PostServiceImpl(private val streamsProducer: StreamsProducer) : PostService {

    override fun createPost(createPostRequestDto: CreatePostRequestDto, userId: String) {
        val uuid: String = UUID.randomUUID().toString()
        val now = Instant.now()
        val createdAt = Timestamp.newBuilder().setSeconds(now.epochSecond).setNanos(now.nano).build()

        val messageKey: PostCdcKey = PostCdcKey.newBuilder()
            .setId(uuid)
            .build()

        val message: PostCdcMessage = PostCdcMessage.newBuilder()
            .setId(uuid)
            .setCreatedAt(createdAt)
            .setCreatorId(userId)
            .setCreatedAt(createdAt)
            .setUpdatedAt(createdAt)
            .setContent(createPostRequestDto.content)
            .setTitle(createPostRequestDto.title)
            .setIsDeleted(false)
            .build()

        streamsProducer.publish(
            KafkaTopic.POST_CREATED_TOPIC, KeyValue.pair(messageKey, message), ProtobufSerde(PostCdcKey.parser()),
            ProtobufSerde(PostCdcMessage.parser())
        )
    }
}