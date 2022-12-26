package com.scs.apps.twitt.function

import com.scs.apps.twitt.*
import com.scs.apps.twitt.constant.EnrichedCommentKStream
import com.scs.apps.twitt.constant.EnrichedPostKStream
import com.scs.apps.twitt.constant.KafkaTopic
import com.scs.apps.twitt.group.CommentGroup
import com.scs.apps.twitt.group.PostGroup
import com.scs.apps.twitt.joiner.PostJoiner
import com.scs.apps.twitt.serde.EnrichedCommentSerde
import com.scs.apps.twitt.serde.EnrichedPostSerde
import org.apache.kafka.streams.*
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.kstream.Produced
import org.apache.kafka.streams.test.TestRecord
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.kotlin.anyOrNull
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.mock.mockito.SpyBean
import java.util.*

@SpringBootTest(classes = [PostJoinerFunction::class])
class PostJoinerFunctionTest {

    @Autowired
    lateinit var postJoinerFunction: PostJoinerFunction

    @MockBean
    lateinit var postJoiner: PostJoiner

    @SpyBean
    lateinit var commentGroup: CommentGroup

    @SpyBean
    lateinit var enrichedCommentSerde: EnrichedCommentSerde

    @SpyBean
    lateinit var postGroup: PostGroup

    @SpyBean
    lateinit var enrichedPostSerde: EnrichedPostSerde

    lateinit var topologyTestDriver: TopologyTestDriver
    lateinit var postUserJoinedTopic: TestInputTopic<EnrichedPostKey, EnrichedPostMessage>
    lateinit var enrichedCommentTopic: TestInputTopic<EnrichedCommentKey, EnrichedCommentMessage>

    lateinit var enrichedPostTopic: TestOutputTopic<EnrichedPostKey, EnrichedPostMessage>

    @BeforeEach
    fun setup() {
        val streamsBuilder = StreamsBuilder()

        val enrichedCommentKStream: EnrichedCommentKStream = streamsBuilder.stream(
            KafkaTopic.COMMENT_USER_JOINED_TOPIC, Consumed.with(
                enrichedCommentSerde.enrichedCommentKeySerde(), enrichedCommentSerde.enrichedCommentMessageSerde()
            )
        )
        val postUserJoinedKStream: EnrichedPostKStream = streamsBuilder.stream(
            KafkaTopic.POST_USER_JOINED_TOPIC, Consumed.with(
                enrichedPostSerde.enrichedPostKeySerde(), enrichedPostSerde.enrichedPostMessageSerde()
            )
        )

        val postJoinedKStream: EnrichedPostKStream =
            postJoinerFunction.joinPost().apply(enrichedCommentKStream).apply(postUserJoinedKStream)
        postJoinedKStream.to(
            KafkaTopic.POST_JOINED, Produced.with(
                enrichedPostSerde.enrichedPostKeySerde(), enrichedPostSerde.enrichedPostMessageSerde()
            )
        )

        val props = Properties()
        props[StreamsConfig.APPLICATION_ID_CONFIG] = "TopologyTestDriver"
        props[StreamsConfig.BOOTSTRAP_SERVERS_CONFIG] = "ignored"

        topologyTestDriver = TopologyTestDriver(streamsBuilder.build(), props)
        enrichedCommentTopic = topologyTestDriver.createInputTopic(
            KafkaTopic.COMMENT_USER_JOINED_TOPIC,
            enrichedCommentSerde.enrichedCommentKeySerde().serializer(),
            enrichedCommentSerde.enrichedCommentMessageSerde().serializer()
        )
        postUserJoinedTopic = topologyTestDriver.createInputTopic(
            KafkaTopic.POST_USER_JOINED_TOPIC,
            enrichedPostSerde.enrichedPostKeySerde().serializer(),
            enrichedPostSerde.enrichedPostMessageSerde().serializer()
        )
        enrichedPostTopic = topologyTestDriver.createOutputTopic(
            KafkaTopic.POST_JOINED,
            enrichedPostSerde.enrichedPostKeySerde().deserializer(),
            enrichedPostSerde.enrichedPostMessageSerde().deserializer()
        )
    }

    @AfterEach
    fun cleanup() {
        topologyTestDriver.close()
    }

    @Test
    fun testJoinPost() {
        `when`(postJoiner.foreignKeyExtractor(anyOrNull())).thenReturn(UserKey.newBuilder().build())

        postUserJoinedTopic.pipeInput(
            TestRecord(
                createEnrichedPostKey(), createEnrichedPostMessage(emptyList())
            )
        )

        enrichedCommentTopic.pipeInput(
            TestRecord(
                createEnrichedCommentKey(), createEnrichedCommentMessage("comment 1", "commentId1")
            )
        )

        enrichedCommentTopic.pipeInput(
            TestRecord(
                createEnrichedCommentKey(), createEnrichedCommentMessage("comment 2", "commentId2")
            )
        )

        assertFalse(enrichedPostTopic.isEmpty)

        val results: MutableList<KeyValue<EnrichedPostKey, EnrichedPostMessage>> =
            enrichedPostTopic.readKeyValuesToList()
        val regularPostMessage: KeyValue<EnrichedPostKey, EnrichedPostMessage> = results[0]
        assertEquals(
            regularPostMessage, KeyValue.pair(
                createEnrichedPostKey(),
                createEnrichedPostMessage(emptyList())
            )
        )

        val postWithFirstCommentMessage: KeyValue<EnrichedPostKey, EnrichedPostMessage> = results[1]
        assertEquals(
            postWithFirstCommentMessage, KeyValue.pair(
                createEnrichedPostKey(),
                createEnrichedPostMessage(listOf(createEnrichedCommentMessage("comment 1", "commentId1")))
            )
        )

        val expected: KeyValue<EnrichedPostKey, EnrichedPostMessage> = KeyValue.pair(
            createEnrichedPostKey(),
            createEnrichedPostMessage(
                listOf(
                    createEnrichedCommentMessage("comment 1", "commentId1"),
                    createEnrichedCommentMessage("comment 2", "commentId2")
                )
            )
        )

        val actual: KeyValue<EnrichedPostKey, EnrichedPostMessage> = results[results.size - 1]

        assertNotNull(actual)
        assertEquals(expected, actual)
    }

    private fun createEnrichedCommentKey(): EnrichedCommentKey {
        return EnrichedCommentKey.newBuilder().setId("commentId").build()
    }

    private fun createEnrichedPostKey(): EnrichedPostKey {
        return EnrichedPostKey.newBuilder().setId("postId").build()
    }

    private fun createEnrichedCommentMessage(comment: String, id: String): EnrichedCommentMessage {
        return EnrichedCommentMessage.newBuilder().setComment(comment).setId(id).setUserId("userId")
            .setUser(createUserMessage()).setPostId("postId").setUser(createUserMessage()).build()
    }

    private fun createEnrichedPostMessage(
        comments: List<EnrichedCommentMessage>
    ): EnrichedPostMessage {
        val builder: EnrichedPostMessage.Builder = EnrichedPostMessage.newBuilder().setId("postId").setTitle("title")
            .setContent("content").setCreatorId("userId").setUser(createUserMessage())
        builder.addAllComments(comments)

        return builder.build()
    }

    private fun createUserMessage(): UserMessage {
        return UserMessage.newBuilder().setId("userId").setName("name").build()
    }
}