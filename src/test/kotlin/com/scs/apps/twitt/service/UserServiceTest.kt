package com.scs.apps.twitt.service

import com.google.protobuf.Timestamp
import com.scs.apps.twitt.UserKey
import com.scs.apps.twitt.UserMessage
import com.scs.apps.twitt.dto.RequestDto
import com.scs.apps.twitt.producer.StreamsProducer
import com.scs.apps.twitt.serde.ProtobufSerde
import com.scs.apps.twitt.serde.UserSerde
import com.scs.apps.twitt.service.impl.UserServiceImpl
import com.scs.apps.twitt.utils.DateTimeUtils
import org.apache.kafka.streams.KeyValue
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Captor
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.capture
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.mock.mockito.SpyBean

@SpringBootTest(classes = [UserServiceImpl::class])
class UserServiceTest {

    @Autowired
    lateinit var userService: UserServiceImpl

    @MockBean
    lateinit var streamsProducer: StreamsProducer

    @SpyBean
    lateinit var userSerde: UserSerde

    @MockBean
    lateinit var dateTimeUtils: DateTimeUtils

    @Captor
    lateinit var keyValueCaptor: ArgumentCaptor<KeyValue<UserKey, UserMessage>>

    @Test
    fun testUpdateUser() {
        val now: Timestamp = Timestamp.newBuilder().build()
        Mockito.`when`(dateTimeUtils.now()).thenReturn(now)

        val requestDto: RequestDto.UpdateUserRequestDto = RequestDto.UpdateUserRequestDto("name")
        userService.updateUser(requestDto, "id")

        val key: UserKey = UserKey.newBuilder()
            .setId("id")
            .build()

        val message: UserMessage = UserMessage.newBuilder()
            .setId("id")
            .setUpdatedAt(now)
            .setName("name")
            .build()

        verify(streamsProducer).publish(
            anyString(), capture(keyValueCaptor),
            any<ProtobufSerde<UserKey>>(),
            any<ProtobufSerde<UserMessage>>()
        )

        Assertions.assertEquals(key, keyValueCaptor.value.key)
        Assertions.assertEquals(message, keyValueCaptor.value.value)
    }
}