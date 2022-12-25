package com.scs.apps.twitt.joiner

import com.google.protobuf.Timestamp
import com.scs.apps.twitt.EnrichedPostMessage
import com.scs.apps.twitt.PostKey
import com.scs.apps.twitt.PostMessage
import com.scs.apps.twitt.UserKey
import com.scs.apps.twitt.UserMessage
import com.scs.apps.twitt.serde.EnrichedPostSerde
import com.scs.apps.twitt.serde.PostSerde
import org.apache.kafka.common.utils.Bytes
import org.apache.kafka.streams.kstream.Materialized
import org.apache.kafka.streams.state.KeyValueStore
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.SpyBean
import java.util.stream.Stream

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = [PostJoiner::class])
class PostJoinerTest {

    @Autowired
    lateinit var postJoiner: PostJoiner

    @SpyBean
    lateinit var postSerde: PostSerde

    @SpyBean
    lateinit var enrichedPostSerde: EnrichedPostSerde

    val createdAt: Timestamp = Timestamp.getDefaultInstance()

    fun parametersToTestJoinPostWithUser(): Stream<Arguments> {
        return Stream.of(
            Arguments.of(
                createPostMessage("title 1", "content 1"), createUserMessage(false, "id 2"),
                EnrichedPostMessage.newBuilder().setCreatedAt(createdAt).setUpdatedAt(createdAt)
                    .setId("id 1").setTitle("title 1").setContent("content 1")
                    .setUser(createUserMessage(false, "id 2"))
                    .setCreatorId("id 2").build()
            ),
            Arguments.of(
                createPostMessage("title 2", "content 2"), createUserMessage(true, "id 2"),
                EnrichedPostMessage.newBuilder().setCreatedAt(createdAt).setUpdatedAt(createdAt)
                    .setId("id 1").setTitle("title 2").setContent("content 2").build()
            )
        )
    }

    @ParameterizedTest
    @MethodSource("parametersToTestJoinPostWithUser")
    fun testJoinPostWithUser(
        postMessage: PostMessage,
        userMessage: UserMessage?,
        expected: EnrichedPostMessage
    ) {
        val actual: EnrichedPostMessage = postJoiner.joinPostWithUser(postMessage, userMessage)

        assertEquals(expected, actual)
    }

    fun createPostMessage(title: String, content: String): PostMessage {
        return PostMessage.newBuilder().setTitle(title).setContent(content).setId("id 1").build()
    }

    fun createUserMessage(isNull: Boolean, id: String): UserMessage? {
        if (isNull) {
            return null
        }

        return UserMessage.newBuilder().setId(id).setCreatedAt(createdAt).setUpdatedAt(createdAt).build()
    }

    @Test
    fun testMaterialized() {
        assertNotNull(postJoiner.materialized())
    }

    @Test
    fun testForeignKeyExtractor() {
        val expected: UserKey = UserKey.newBuilder().setId("id").build()

        assertEquals(expected, postJoiner.foreignKeyExtractor(PostMessage.newBuilder().setCreatorId("id").build()))
    }
}