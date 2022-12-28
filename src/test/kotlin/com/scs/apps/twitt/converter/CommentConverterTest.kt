package com.scs.apps.twitt.converter

import com.google.protobuf.Timestamp
import com.scs.apps.twitt.EnrichedCommentMessage
import com.scs.apps.twitt.UserMessage
import com.scs.apps.twitt.entity.Author
import com.scs.apps.twitt.entity.Comment
import com.scs.apps.twitt.utils.DateTimeUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import java.time.ZonedDateTime
import java.util.*

@SpringBootTest(classes = [CommentConverter::class])
class CommentConverterTest {

    @MockBean
    lateinit var dateTimeUtils: DateTimeUtils

    @Autowired
    lateinit var commentConverter: CommentConverter

    @Test
    fun testConvertToEntity() {
        val now = Timestamp.getDefaultInstance()
        val zdtNow = ZonedDateTime.now()
        val commentMessage: EnrichedCommentMessage = EnrichedCommentMessage.newBuilder()
            .setComment("comment")
            .setId("ee79b267-2bf0-422a-a066-9fa205efba1d")
            .setPostId("e3b07964-09a3-4292-86e0-69741f5f9a7b")
            .setCreatedAt(now)
            .setUpdatedAt(now)
            .setUserId("2f08c851-b403-4bdf-aff0-f848ba2a6ef5")
            .setUser(
                UserMessage.newBuilder().setId("2f08c851-b403-4bdf-aff0-f848ba2a6ef5")
                    .setName("author").setProfilePic("pic").build()
            )
            .build()

        `when`(dateTimeUtils.parseToZdt(now)).thenReturn(zdtNow)

        val actual: Comment = commentConverter.convertToEntity(commentMessage)
        val author = Author("author", "pic")
        author.id = UUID.fromString("2f08c851-b403-4bdf-aff0-f848ba2a6ef5")

        val expected = Comment("comment", "e3b07964-09a3-4292-86e0-69741f5f9a7b", author)
        expected.updatedAt = zdtNow
        expected.createdAt = zdtNow
        expected.id = UUID.fromString("ee79b267-2bf0-422a-a066-9fa205efba1d")

        assertThat(actual).usingRecursiveComparison().isEqualTo(expected)
    }
}