package com.scs.apps.twitt.service

import com.google.protobuf.Timestamp
import com.google.protobuf.util.Timestamps
import com.scs.apps.twitt.EnrichedPostMessage
import com.scs.apps.twitt.converter.PostConverter
import com.scs.apps.twitt.entity.Post
import com.scs.apps.twitt.repository.PostEsRepository
import com.scs.apps.twitt.service.impl.PostEventServiceImpl
import com.scs.apps.twitt.utils.DateTimeUtils
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.*
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.never
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import java.time.ZonedDateTime
import java.util.*
import java.util.stream.Stream

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = [PostEventServiceImpl::class])
class PostEventServiceTest {

    @Autowired
    lateinit var postEventService: PostEventServiceImpl

    @MockBean
    lateinit var dateTimeUtils: DateTimeUtils

    @MockBean
    lateinit var postEsRepository: PostEsRepository

    @MockBean
    lateinit var postConverter: PostConverter

    val nowTimestamp: Timestamp = Timestamp.getDefaultInstance()
    val nowZdt: ZonedDateTime = ZonedDateTime.now()

    fun parametersToTestUpsert(): Stream<Arguments> {
        return Stream.of(
            Arguments.of(
                EnrichedPostMessage.newBuilder().setId("390a81fb-39cc-4b81-b944-92972a595ab6")
                    .setCreatedAt(nowTimestamp).setUpdatedAt(nowTimestamp).build(), true
            ),
            Arguments.of(EnrichedPostMessage.newBuilder().setId("390a81fb-39cc-4b81-b944-92972a595ab6").build(), false)
        )
    }

    @ParameterizedTest
    @MethodSource("parametersToTestUpsert")
    fun testUpsert(postMessage: EnrichedPostMessage, isMessageIgnored: Boolean) {
        val post = Post()
        post.updatedAt = nowZdt
        if (isMessageIgnored) {
            `when`(postEsRepository.findById(eq(UUID.fromString("390a81fb-39cc-4b81-b944-92972a595ab6"))))
                .thenReturn(Optional.of(post))
            `when`(dateTimeUtils.parseToTimestamp(anyOrNull())).thenReturn(Timestamps.fromSeconds(1L))
            `when`(postConverter.toEntity(anyOrNull())).thenReturn(Post())
        } else {
            `when`(postEsRepository.findById(eq(UUID.fromString("390a81fb-39cc-4b81-b944-92972a595ab6"))))
                .thenReturn(Optional.empty())
        }

        postEventService.upsert(postMessage)

        if (isMessageIgnored) {
            verify(postConverter, never()).toEntity(anyOrNull())
            verify(postEsRepository, never()).save(anyOrNull())
        } else {
            verify(postEsRepository).save(anyOrNull())
        }
    }
}