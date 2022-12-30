package com.scs.apps.twitt.service

import com.google.protobuf.Timestamp
import com.scs.apps.twitt.ActivityActionType
import com.scs.apps.twitt.ActivityKey
import com.scs.apps.twitt.ActivityMessage
import com.scs.apps.twitt.dto.RequestDto.RatePostRequestDto
import com.scs.apps.twitt.entity.Post
import com.scs.apps.twitt.exception.NotFoundException
import com.scs.apps.twitt.producer.StreamsProducer
import com.scs.apps.twitt.repository.PostEsRepository
import com.scs.apps.twitt.serde.ActivitySerde
import com.scs.apps.twitt.serde.ProtobufSerde
import com.scs.apps.twitt.service.impl.ActivityServiceImpl
import com.scs.apps.twitt.utils.DateTimeUtils
import com.scs.apps.twitt.utils.UuidUtils
import org.apache.kafka.streams.KeyValue
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mockito.anyString
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.capture
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.mock.mockito.SpyBean
import java.util.*
import java.util.stream.Stream

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = [ActivityServiceImpl::class])
class ActivityServiceTest {

    @MockBean
    lateinit var postRepository: PostEsRepository

    @MockBean
    lateinit var streamsProducer: StreamsProducer

    @SpyBean
    lateinit var activitySerde: ActivitySerde

    @MockBean
    lateinit var uuidUtils: UuidUtils

    @MockBean
    lateinit var dateTimeUtils: DateTimeUtils

    @Autowired
    lateinit var activityService: ActivityServiceImpl

    @Captor
    lateinit var activityCaptor: ArgumentCaptor<KeyValue<ActivityKey, ActivityMessage>>

    fun parametersToTestRatePost(): Stream<Arguments> {
        return Stream.of(
            Arguments.of(
                RatePostRequestDto(UUID.fromString("f661044e-398b-4079-92f9-e3c2134aec5d"), "like"),
                ActivityActionType.POST_LIKED
            ),
            Arguments.of(
                RatePostRequestDto(UUID.fromString("f661044e-398b-4079-92f9-e3c2134aec5d"), "dislike"),
                ActivityActionType.POST_DISLIKED
            )
        )
    }

    @ParameterizedTest
    @MethodSource("parametersToTestRatePost")
    fun testRatePost(requestDto: RatePostRequestDto, actionType: ActivityActionType) {
        val post = Post(title = "title", content = "content")
        post.id = UUID.fromString("f661044e-398b-4079-92f9-e3c2134aec5d")

        val nowTimestamp: Timestamp = Timestamp.getDefaultInstance()
        `when`(dateTimeUtils.now()).thenReturn(nowTimestamp)
        `when`(postRepository.findById(UUID.fromString("f661044e-398b-4079-92f9-e3c2134aec5d")))
            .thenReturn(Optional.of(post))
        `when`(uuidUtils.randomUuid()).thenReturn(UUID.fromString("e5ea3529-6b46-4469-8796-9f92c5969dce"))

        activityService.ratePost(requestDto)

        verify(streamsProducer).publish(
            anyString(), capture(activityCaptor),
            any<ProtobufSerde<ActivityKey>>(),
            any<ProtobufSerde<ActivityMessage>>()
        )

        val activityMessage: ActivityMessage = ActivityMessage.newBuilder()
            .setId("e5ea3529-6b46-4469-8796-9f92c5969dce")
            .setPostId("f661044e-398b-4079-92f9-e3c2134aec5d")
            .setActionType(actionType)
            .setSyncAt(nowTimestamp)
            .build()

        val activityKey: ActivityKey = ActivityKey.newBuilder().setId("e5ea3529-6b46-4469-8796-9f92c5969dce").build()
        val expected: KeyValue<ActivityKey, ActivityMessage> = KeyValue.pair(activityKey, activityMessage)

        assertEquals(expected.key, activityCaptor.value.key)
        assertEquals(expected.value, activityCaptor.value.value)
    }

    @Test
    fun testRatePostWhenPostNotFoundThenThrowNotFoundException() {
        `when`(postRepository.findById(UUID.fromString("f661044e-398b-4079-92f9-e3c2134aec5d")))
            .thenReturn(Optional.empty())

        val actual: NotFoundException = assertThrows(NotFoundException::class.java) {
            activityService.ratePost(
                RatePostRequestDto(
                    UUID.fromString("f661044e-398b-4079-92f9-e3c2134aec5d"),
                    "like"
                )
            )
        }

        assertEquals("Post with id f661044e-398b-4079-92f9-e3c2134aec5d is not found.", actual.message)
        verifyNoInteractions(streamsProducer)
    }
}