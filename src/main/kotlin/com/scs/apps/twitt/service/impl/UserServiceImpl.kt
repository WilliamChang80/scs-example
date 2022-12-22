package com.scs.apps.twitt.service.impl

import com.google.protobuf.Timestamp
import com.scs.apps.twitt.UserKey
import com.scs.apps.twitt.UserMessage
import com.scs.apps.twitt.constant.KafkaTopic
import com.scs.apps.twitt.dto.RequestDto
import com.scs.apps.twitt.producer.StreamsProducer
import com.scs.apps.twitt.serde.UserSerde
import com.scs.apps.twitt.service.UserService
import org.apache.kafka.streams.KeyValue
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class UserServiceImpl(private val streamsProducer: StreamsProducer, private val userSerde: UserSerde) : UserService {

    override fun updateUser(updateUserRequestDto: RequestDto.UpdateUserRequestDto, userId: String) {
        val now = Instant.now()
        val createdAt = Timestamp.newBuilder().setSeconds(now.epochSecond).setNanos(now.nano).build()

        val userKey: UserKey = UserKey.newBuilder()
            .setId(userId)
            .build()

        val (name) = updateUserRequestDto
        val userMessage: UserMessage = UserMessage.newBuilder()
            .setId(userId)
            .setName(name)
            .setUpdatedAt(createdAt)
            .build()

        streamsProducer.publish(
            KafkaTopic.USER_UPDATED_TOPIC, KeyValue.pair(userKey, userMessage),
            userSerde.userKeySerde(), userSerde.userMessageSerde()
        )
    }
}