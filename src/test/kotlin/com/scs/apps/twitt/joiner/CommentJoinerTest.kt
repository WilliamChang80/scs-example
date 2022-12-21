package com.scs.apps.twitt.joiner

import com.google.protobuf.Timestamp
import com.scs.apps.twitt.Comment.CommentMessage
import com.scs.apps.twitt.EnrichedCommentMessage
import com.scs.apps.twitt.UserMessage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.util.stream.Stream

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = [CommentJoiner::class])
class CommentJoinerTest {

    @Autowired
    lateinit var commentJoiner: CommentJoiner

    val createdAt: Timestamp = Timestamp.getDefaultInstance()

    fun parametersToTestJoinCommentWithUser(): Stream<Arguments> {
        return Stream.of(
            Arguments.of(
                createCommentMessage("comment 1"), createUserMessage(false, "id 2"),
                EnrichedCommentMessage.newBuilder().setCreatedAt(createdAt).setUpdatedAt(createdAt)
                    .setId("id 1").setComment("comment 1")
                    .setUser(createUserMessage(false, "id 2"))
                    .setUserId("id 2").build()
            ),
            Arguments.of(
                createCommentMessage("comment 2"), createUserMessage(true, "id 2"),
                EnrichedCommentMessage.newBuilder().setCreatedAt(createdAt).setUpdatedAt(createdAt)
                    .setId("id 1").setComment("comment 2").build()
            )
        )
    }

    @ParameterizedTest
    @MethodSource("parametersToTestJoinCommentWithUser")
    fun testJoinCommentWithUser(
        commentMessage: CommentMessage,
        userMessage: UserMessage?,
        expected: EnrichedCommentMessage
    ) {
//        val actual: EnrichedCommentMessage = commentJoiner.joinCommentWithUser().apply(commentMessage, userMessage)
//
//        assertEquals(expected, actual)
    }

    fun createCommentMessage(comment: String): CommentMessage {
        return CommentMessage.newBuilder().setComment(comment).setId("id 1").build()
    }

    fun createUserMessage(isNull: Boolean, id: String): UserMessage? {
        if (isNull) {
            return null
        }

        return UserMessage.newBuilder().setId(id).setCreatedAt(createdAt).setUpdatedAt(createdAt).build()
    }
}