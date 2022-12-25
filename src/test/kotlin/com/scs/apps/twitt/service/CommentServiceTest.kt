package com.scs.apps.twitt.service

import com.google.protobuf.Timestamp
import com.scs.apps.twitt.Comment.CommentKey
import com.scs.apps.twitt.Comment.CommentMessage
import com.scs.apps.twitt.dto.RequestDto
import com.scs.apps.twitt.producer.StreamsProducer
import com.scs.apps.twitt.serde.CommentSerde
import com.scs.apps.twitt.serde.ProtobufSerde
import com.scs.apps.twitt.service.impl.CommentServiceImpl
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

@SpringBootTest(classes = [CommentServiceImpl::class])
class CommentServiceTest {

    @Autowired
    lateinit var commentService: CommentServiceImpl

    @MockBean
    lateinit var streamsProducer: StreamsProducer

    @SpyBean
    lateinit var commentSerde: CommentSerde

    @MockBean
    lateinit var uuidUtils: UuidUtils

    @MockBean
    lateinit var dateTimeUtils: DateTimeUtils

    @Captor
    lateinit var keyValueCaptor: ArgumentCaptor<KeyValue<CommentKey, CommentMessage>>

    @Test
    fun testCreateComment() {
        val now: Timestamp = Timestamp.newBuilder().build()
        `when`(uuidUtils.randomUuid()).thenReturn(UUID.fromString("8a3aa38d-b365-4ef7-a2db-fcf5afcb70f3"))
        `when`(dateTimeUtils.now()).thenReturn(now)

        val requestDto: RequestDto.CreateCommentRequestDto = RequestDto.CreateCommentRequestDto(
            "comment",
            "postId"
        )
        commentService.createComment(requestDto, "id")

        val key: CommentKey = CommentKey.newBuilder()
            .setId("8a3aa38d-b365-4ef7-a2db-fcf5afcb70f3")
            .build()

        val message: CommentMessage = CommentMessage.newBuilder()
            .setId("8a3aa38d-b365-4ef7-a2db-fcf5afcb70f3")
            .setPostId("postId")
            .setCreatedAt(now)
            .setUpdatedAt(now)
            .setComment("comment")
            .setUserId("id")
            .build()

        verify(streamsProducer).publish(
            anyString(), capture(keyValueCaptor),
            any<ProtobufSerde<CommentKey>>(),
            any<ProtobufSerde<CommentMessage>>()
        )

        assertEquals(key, keyValueCaptor.value.key)
        assertEquals(message, keyValueCaptor.value.value)
    }
}