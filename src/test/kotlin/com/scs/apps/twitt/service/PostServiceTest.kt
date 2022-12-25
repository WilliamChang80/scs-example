package com.scs.apps.twitt.service

import com.google.protobuf.Timestamp
import com.scs.apps.twitt.PostCdcKey
import com.scs.apps.twitt.PostCdcMessage
import com.scs.apps.twitt.dto.RequestDto
import com.scs.apps.twitt.producer.StreamsProducer
import com.scs.apps.twitt.serde.PostCdcSerde
import com.scs.apps.twitt.serde.ProtobufSerde
import com.scs.apps.twitt.service.impl.PostServiceImpl
import com.scs.apps.twitt.utils.DateTimeUtils
import com.scs.apps.twitt.utils.UuidUtils
import org.apache.kafka.streams.KeyValue
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mockito.anyString
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.capture
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.mock.mockito.SpyBean
import java.util.*

@SpringBootTest(classes = [PostServiceImpl::class])
class PostServiceTest {

    @Autowired
    lateinit var postService: PostServiceImpl

    @MockBean
    lateinit var streamsProducer: StreamsProducer

    @MockBean
    lateinit var uuidUtils: UuidUtils

    @MockBean
    lateinit var dateTimeUtils: DateTimeUtils

    @SpyBean
    lateinit var postCdcSerde: PostCdcSerde

    @Captor
    lateinit var keyValueCaptor: ArgumentCaptor<KeyValue<PostCdcKey, PostCdcMessage>>

    @Test
    fun testCreatePost() {
        val now: Timestamp = Timestamp.newBuilder().build()
        `when`(uuidUtils.randomUuid()).thenReturn(UUID.fromString("8a3aa38d-b365-4ef7-a2db-fcf5afcb70f2"))
        `when`(dateTimeUtils.now()).thenReturn(now)

        val requestDto: RequestDto.CreatePostRequestDto = RequestDto.CreatePostRequestDto("title", "content")
        postService.createPost(requestDto, "id")

        val key: PostCdcKey = PostCdcKey.newBuilder()
            .setId("8a3aa38d-b365-4ef7-a2db-fcf5afcb70f2")
            .build()

        val message: PostCdcMessage = PostCdcMessage.newBuilder().setId("8a3aa38d-b365-4ef7-a2db-fcf5afcb70f2")
            .setCreatorId("id")
            .setCreatedAt(now)
            .setUpdatedAt(now)
            .setContent("content")
            .setTitle("title")
            .setIsDeleted(false)
            .build()

        verify(streamsProducer).publish(
            anyString(), capture(keyValueCaptor),
            any<ProtobufSerde<PostCdcKey>>(),
            any<ProtobufSerde<PostCdcMessage>>()
        )

        assertEquals(key, keyValueCaptor.value.key)
        assertEquals(message, keyValueCaptor.value.value)
    }
}