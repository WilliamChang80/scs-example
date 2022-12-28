package com.scs.apps.twitt.converter

import com.google.protobuf.Timestamp
import com.scs.apps.twitt.*
import com.scs.apps.twitt.entity.Author
import com.scs.apps.twitt.entity.Comment
import com.scs.apps.twitt.entity.Post
import com.scs.apps.twitt.utils.DateTimeUtils
import org.apache.kafka.streams.KeyValue
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
import org.mockito.kotlin.times
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import java.time.ZonedDateTime
import java.util.*

@SpringBootTest(classes = [PostConverter::class])
class PostConverterTest {

    @MockBean
    lateinit var dateTimeUtils: DateTimeUtils

    @MockBean
    lateinit var commentConverter: CommentConverter

    @Autowired
    lateinit var postConverter: PostConverter

    @Test
    fun testToEntity() {
        val now = Timestamp.getDefaultInstance()
        val zdtNow = ZonedDateTime.now()

        val enrichedPostMessage: EnrichedPostMessage = EnrichedPostMessage.newBuilder()
            .setId("e3b07964-09a3-4292-86e0-69741f5f9a7b")
            .setCreatorId("ac8401cb-921f-41fe-8f5c-752f2f00982b")
            .setContent("content")
            .setTitle("title")
            .setUser(
                UserMessage.newBuilder().setId("2f08c851-b403-4bdf-aff0-f848ba2a6ef5")
                    .setName("author").setProfilePic("pic").build()
            )
            .setCreatedAt(now)
            .setUpdatedAt(now)
            .addAllComments(
                listOf(
                    EnrichedCommentMessage.newBuilder().setComment("comment 1").build(),
                    EnrichedCommentMessage.newBuilder().setComment("comment 1").build()
                )
            )
            .build()

        `when`(dateTimeUtils.parseToZdt(now)).thenReturn(zdtNow)
        `when`(
            commentConverter.convertToEntity(eq(EnrichedCommentMessage.newBuilder().setComment("comment 1").build()))
        ).thenReturn(Comment())

        val actual: Post = postConverter.toEntity(enrichedPostMessage)
        val author = Author("author", "pic")
        author.id = UUID.fromString("2f08c851-b403-4bdf-aff0-f848ba2a6ef5")

        val expected = Post(
            content = "content", title = "title", creator = author, comments =
            listOf(Comment(), Comment())
        )
        expected.id = UUID.fromString("e3b07964-09a3-4292-86e0-69741f5f9a7b")
        expected.createdAt = zdtNow
        expected.updatedAt = zdtNow

        assertThat(actual).usingRecursiveComparison().isEqualTo(expected)

        verify(commentConverter, times(2)).convertToEntity(anyOrNull())
    }

    @Test
    fun testToPostCdcMessage() {
        val now = Timestamp.getDefaultInstance()
        val zdtNow = ZonedDateTime.now()

        val author = Author("author", "pic")
        author.id = UUID.fromString("2f08c851-b403-4bdf-aff0-f848ba2a6ef5")

        val post = Post(
            content = "content", title = "title", creator = author
        )
        post.id = UUID.fromString("cfde02ad-72c7-4514-aa64-4d48949c58a7")
        post.createdAt = zdtNow
        post.updatedAt = zdtNow

        `when`(dateTimeUtils.parseToTimestamp(zdtNow)).thenReturn(now)

        val actual: KeyValue<PostCdcKey, PostCdcMessage> = postConverter.toPostCdcMessage(post)
        val expected: KeyValue<PostCdcKey, PostCdcMessage> = KeyValue.pair(
            PostCdcKey.newBuilder().setId("cfde02ad-72c7-4514-aa64-4d48949c58a7").build(),
            PostCdcMessage.newBuilder().setId("cfde02ad-72c7-4514-aa64-4d48949c58a7").setTitle("title")
                .setContent("content").setCreatorId("2f08c851-b403-4bdf-aff0-f848ba2a6ef5").setCreatedAt(now)
                .setUpdatedAt(now).build()
        )

        assertEquals(expected, actual)
    }
}