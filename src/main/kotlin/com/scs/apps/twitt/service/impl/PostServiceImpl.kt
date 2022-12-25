package com.scs.apps.twitt.service.impl

import com.scs.apps.twitt.PostCdcKey
import com.scs.apps.twitt.PostCdcMessage
import com.scs.apps.twitt.constant.KafkaTopic
import com.scs.apps.twitt.dto.RequestDto
import com.scs.apps.twitt.producer.StreamsProducer
import com.scs.apps.twitt.serde.PostCdcSerde
import com.scs.apps.twitt.service.PostService
import com.scs.apps.twitt.utils.DateTimeUtils
import com.scs.apps.twitt.utils.UuidUtils
import org.apache.kafka.streams.KeyValue
import org.springframework.stereotype.Service


@Service
class PostServiceImpl(
    private val streamsProducer: StreamsProducer, private val postCdcSerde: PostCdcSerde,
    private val uuidUtils: UuidUtils, private val dateTimeUtils: DateTimeUtils
) : PostService {

    override fun createPost(createPostRequestDto: RequestDto.CreatePostRequestDto, userId: String) {
        val uuid: String = uuidUtils.randomUuid().toString()
        val createdAt = dateTimeUtils.now()

        val messageKey: PostCdcKey = PostCdcKey.newBuilder()
            .setId(uuid)
            .build()

        val message: PostCdcMessage = PostCdcMessage.newBuilder()
            .setId(uuid)
            .setCreatorId(userId)
            .setCreatedAt(createdAt)
            .setUpdatedAt(createdAt)
            .setContent(createPostRequestDto.content)
            .setTitle(createPostRequestDto.title)
            .setIsDeleted(false)
            .build()

        streamsProducer.publish(
            KafkaTopic.POST_CREATED_TOPIC, KeyValue.pair(messageKey, message), postCdcSerde.postCdcKeySerde(),
            postCdcSerde.postCdcMessageSerde()
        )
    }
}