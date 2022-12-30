package com.scs.apps.twitt.service.impl

import com.scs.apps.twitt.ActivityActionType
import com.scs.apps.twitt.ActivityKey
import com.scs.apps.twitt.ActivityMessage
import com.scs.apps.twitt.constant.KafkaTopic
import com.scs.apps.twitt.dto.RequestDto
import com.scs.apps.twitt.entity.Post
import com.scs.apps.twitt.exception.NotFoundException
import com.scs.apps.twitt.producer.StreamsProducer
import com.scs.apps.twitt.repository.PostEsRepository
import com.scs.apps.twitt.serde.ActivitySerde
import com.scs.apps.twitt.service.ActivityService
import com.scs.apps.twitt.utils.DateTimeUtils
import com.scs.apps.twitt.utils.UuidUtils
import org.apache.kafka.streams.KeyValue
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class ActivityServiceImpl(
    private val postRepository: PostEsRepository,
    private val streamsProducer: StreamsProducer,
    private val activitySerde: ActivitySerde,
    private val uuidUtils: UuidUtils,
    private val dateTimeUtils: DateTimeUtils
) : ActivityService {

    override fun ratePost(requestDto: RequestDto.RatePostRequestDto) {
        val activityId: UUID = uuidUtils.randomUuid()
        val post: Post = postRepository.findById(requestDto.id)
            .orElseThrow { NotFoundException("Post with id ${requestDto.id} is not found.") }

        val activityMessage: ActivityMessage = ActivityMessage.newBuilder()
            .setPostId(post.id.toString())
            .setId(activityId.toString())
            .setActionType(getActionType(requestDto.type))
            .setSyncAt(dateTimeUtils.now())
            .build()
        val activityKey: ActivityKey = ActivityKey.newBuilder()
            .setId(activityId.toString())
            .build()

        streamsProducer.publish(
            KafkaTopic.ACTIVITY_UPDATED, KeyValue.pair(activityKey, activityMessage),
            activitySerde.activityKeySerde(), activitySerde.activityMessageSerde()
        )
    }

    private fun getActionType(type: String): ActivityActionType {
        if (type == "like") {
            return ActivityActionType.POST_LIKED
        }

        return ActivityActionType.POST_DISLIKED
    }
}