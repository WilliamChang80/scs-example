package com.scs.apps.twitt.service

import com.google.protobuf.Timestamp
import com.scs.apps.twitt.PostCdcKey
import com.scs.apps.twitt.PostCdcMessage
import com.scs.apps.twitt.converter.PostConverter
import com.scs.apps.twitt.dto.RequestDto
import com.scs.apps.twitt.entity.Author
import com.scs.apps.twitt.entity.Post
import com.scs.apps.twitt.exception.NotFoundException
import com.scs.apps.twitt.producer.StreamsProducer
import com.scs.apps.twitt.repository.AuthorJPARepository
import com.scs.apps.twitt.repository.PostJPARepository
import com.scs.apps.twitt.serde.PostCdcSerde
import com.scs.apps.twitt.serde.ProtobufSerde
import com.scs.apps.twitt.service.impl.PostServiceImpl
import com.scs.apps.twitt.utils.DateTimeUtils
import org.apache.kafka.streams.KeyValue
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.eq
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
import java.time.ZonedDateTime
import java.util.*

@SpringBootTest(classes = [PostServiceImpl::class])
class PostServiceTest {

    @Autowired
    lateinit var postService: PostServiceImpl

    @MockBean
    lateinit var streamsProducer: StreamsProducer

    @MockBean
    lateinit var dateTimeUtils: DateTimeUtils

    @MockBean
    lateinit var postRepository: PostJPARepository

    @MockBean
    lateinit var authorRepository: AuthorJPARepository

    @MockBean
    lateinit var postConverter: PostConverter

    @SpyBean
    lateinit var postCdcSerde: PostCdcSerde

    @Captor
    lateinit var keyValueCaptor: ArgumentCaptor<KeyValue<PostCdcKey, PostCdcMessage>>

    @Captor
    lateinit var postCaptor: ArgumentCaptor<Post>

    @Test
    fun testCreatePost() {
        val nowTimestamp: Timestamp = Timestamp.getDefaultInstance()
        val nowZdt = ZonedDateTime.now()
        val author = Author(name = "name")
        author.id = UUID.fromString("f661044e-398b-4079-92f9-e3c2134aec5d")

        `when`(
            authorRepository.findById(
                eq(UUID.fromString("f661044e-398b-4079-92f9-e3c2134aec5d"))
            )
        ).thenReturn(Optional.of(author))

        val updatedPost = Post(title = "title", content = "content", creator = author)
        updatedPost.id = UUID.fromString("8a3aa38d-b365-4ef7-a2db-fcf5afcb70f2")
        updatedPost.createdAt = nowZdt
        updatedPost.updatedAt = nowZdt

        `when`(postConverter.toPostCdcMessage(updatedPost)).thenReturn(
            KeyValue.pair(PostCdcKey.newBuilder().build(), PostCdcMessage.newBuilder().build()))
        `when`(postRepository.save(postCaptor.capture())).thenReturn(updatedPost)
        `when`(dateTimeUtils.parseToTimestamp(nowZdt)).thenReturn(nowTimestamp)

        postService.createPost(
            RequestDto.CreatePostRequestDto("title", "content"),
            "f661044e-398b-4079-92f9-e3c2134aec5d"
        )

        val post = Post(title = "title", content = "content", creator = author)
        assertThat(postCaptor.value).usingRecursiveComparison().isEqualTo(post)

        verify(streamsProducer).publish(
            anyString(), capture(keyValueCaptor),
            any<ProtobufSerde<PostCdcKey>>(),
            any<ProtobufSerde<PostCdcMessage>>()
        )

        assertEquals(PostCdcKey.newBuilder().build(), keyValueCaptor.value.key)
        assertEquals(PostCdcMessage.newBuilder().build(), keyValueCaptor.value.value)
    }

    @Test
    fun testCreatePostWhenUserNotFoundThenThrowNotFoundException() {
        `when`(
            authorRepository.findById(
                eq(UUID.fromString("f661044e-398b-4079-92f9-e3c2134aec5d"))
            )
        ).thenReturn(Optional.empty())

        val actual = assertThrows(NotFoundException::class.java) {
            postService.createPost(
                RequestDto.CreatePostRequestDto("title", "content"),
                "f661044e-398b-4079-92f9-e3c2134aec5d"
            )
        }

        assertEquals("User with id f661044e-398b-4079-92f9-e3c2134aec5d is not found.", actual.message)
    }
}