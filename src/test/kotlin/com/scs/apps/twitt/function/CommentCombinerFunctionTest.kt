package com.scs.apps.twitt.function

import com.scs.apps.twitt.Comment.CommentKey
import com.scs.apps.twitt.Comment.CommentMessage
import com.scs.apps.twitt.EnrichedCommentKey
import com.scs.apps.twitt.EnrichedCommentMessage
import com.scs.apps.twitt.UserKey
import com.scs.apps.twitt.UserMessage
import com.scs.apps.twitt.constant.CommentKTable
import com.scs.apps.twitt.constant.EnrichedCommentKStream
import com.scs.apps.twitt.constant.KafkaTopic
import com.scs.apps.twitt.constant.UserKTable
import com.scs.apps.twitt.joiner.CommentJoiner
import com.scs.apps.twitt.serde.CommentSerde
import com.scs.apps.twitt.serde.EnrichedCommentSerde
import com.scs.apps.twitt.serde.PostSerde
import com.scs.apps.twitt.serde.UserSerde
import org.apache.kafka.streams.*
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.kstream.Produced
import org.apache.kafka.streams.test.TestRecord
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Answers
import org.mockito.Mockito.`when`
import org.mockito.kotlin.anyOrNull
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.mock.mockito.SpyBean
import java.util.*

@SpringBootTest(classes = [CommentJoinerFunction::class])
class CommentCombinerFunctionTest {

    @Autowired
    lateinit var commentCombinerFunction: CommentJoinerFunction

    @SpyBean
    lateinit var commentSerde: CommentSerde

    @SpyBean
    lateinit var enrichedCommentSerde: EnrichedCommentSerde

    @SpyBean
    lateinit var postSerde: PostSerde

    @SpyBean
    lateinit var userSerde: UserSerde

    @MockBean(answer = Answers.RETURNS_DEEP_STUBS)
    lateinit var commentJoiner: CommentJoiner

    lateinit var topologyTestDriver: TopologyTestDriver
    lateinit var commentTopic: TestInputTopic<CommentKey, CommentMessage>
    lateinit var userTopic: TestInputTopic<UserKey, UserMessage>

    lateinit var joinedCommentTopic: TestOutputTopic<EnrichedCommentKey, EnrichedCommentMessage>

    @BeforeEach
    fun setup() {
        val streamsBuilder = StreamsBuilder()

        val commentKTable: CommentKTable = streamsBuilder.table(
            KafkaTopic.COMMENT_CREATED_TOPIC, Consumed.with(
                commentSerde.commentKeySerde(), commentSerde.commentMessageSerde()
            )
        )

        val userKTable: UserKTable = streamsBuilder.table(
            KafkaTopic.USER_UPDATED_TOPIC, Consumed.with(userSerde.userKeySerde(), userSerde.userMessageSerde())
        )

        val enrichedCommentKTable: EnrichedCommentKStream =
            commentCombinerFunction.commentUserJoiner().apply(commentKTable, userKTable)
        enrichedCommentKTable.to(
            KafkaTopic.COMMENT_USER_JOINED_TOPIC, Produced.with(
                enrichedCommentSerde.enrichedCommentKeySerde(), enrichedCommentSerde.enrichedCommentMessageSerde()
            )
        )

        val props = Properties()
        props[StreamsConfig.APPLICATION_ID_CONFIG] = "TopologyTestDriver"
        props[StreamsConfig.BOOTSTRAP_SERVERS_CONFIG] = "ignored"

        topologyTestDriver = TopologyTestDriver(streamsBuilder.build(), props)
        commentTopic = topologyTestDriver.createInputTopic(
            KafkaTopic.COMMENT_CREATED_TOPIC,
            commentSerde.commentKeySerde().serializer(),
            commentSerde.commentMessageSerde().serializer()
        )
        userTopic = topologyTestDriver.createInputTopic(
            KafkaTopic.USER_UPDATED_TOPIC,
            userSerde.userKeySerde().serializer(),
            userSerde.userMessageSerde().serializer()
        )
        joinedCommentTopic = topologyTestDriver.createOutputTopic(
            KafkaTopic.COMMENT_USER_JOINED_TOPIC,
            enrichedCommentSerde.enrichedCommentKeySerde().deserializer(),
            enrichedCommentSerde.enrichedCommentMessageSerde().deserializer()
        )

        `when`(commentJoiner.joinCommentWithUser(anyOrNull(), anyOrNull()))
            .thenReturn(EnrichedCommentMessage.newBuilder().setId("id").setComment("comment").build())
    }

    @AfterEach
    fun cleanup() {
        topologyTestDriver.close()
    }

    @Test
    fun testCommentUserCombiner() {
        commentTopic.pipeInput(createCommentTestRecord("1", "1"))
        commentTopic.pipeInput(createCommentTestRecord("2", "2"))
        userTopic.pipeInput(createUserTestRecord("1"))
        userTopic.pipeInput(createUserTestRecord("2"))

        assertFalse(joinedCommentTopic.isEmpty)

        val expected: List<KeyValue<EnrichedCommentKey, EnrichedCommentMessage>> = listOf(
            KeyValue.pair(createEnrichedCommentKey("1"), createEnrichedCommentMessage("id")),
            KeyValue.pair(createEnrichedCommentKey("2"), createEnrichedCommentMessage("id")),
        )

        val actual: List<KeyValue<EnrichedCommentKey, EnrichedCommentMessage>> =
            joinedCommentTopic.readKeyValuesToList()

        assertEquals(expected, actual)
    }

    fun createEnrichedCommentKey(id: String): EnrichedCommentKey {
        return EnrichedCommentKey.newBuilder().setId(id).build()
    }

    fun createEnrichedCommentMessage(id: String): EnrichedCommentMessage {
        return EnrichedCommentMessage.newBuilder().setId(id).setComment("comment").build()
    }

    fun createCommentTestRecord(id: String, userId: String): TestRecord<CommentKey, CommentMessage> {
        return TestRecord(
            CommentKey.newBuilder().setId(id).build(), CommentMessage.newBuilder().setId(id).setUserId(userId).build()
        )
    }

    fun createUserTestRecord(id: String): TestRecord<UserKey, UserMessage> {
        return TestRecord(UserKey.newBuilder().setId(id).build(), UserMessage.newBuilder().setId(id).build())
    }
}