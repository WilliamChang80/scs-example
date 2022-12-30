package com.scs.apps.twitt.function

import com.scs.apps.twitt.*
import com.scs.apps.twitt.constant.AggregatedActivityKStream
import com.scs.apps.twitt.constant.EnrichedCommentKStream
import com.scs.apps.twitt.constant.EnrichedPostKStream
import com.scs.apps.twitt.constant.KafkaTopic
import com.scs.apps.twitt.group.ActivityGroup
import com.scs.apps.twitt.group.CommentGroup
import com.scs.apps.twitt.group.PostGroup
import com.scs.apps.twitt.joiner.PostJoiner
import com.scs.apps.twitt.serde.ActivitySerde
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
    lateinit var activityGroup: ActivityGroup

    @SpyBean
    lateinit var enrichedCommentSerde: EnrichedCommentSerde

    @SpyBean
    lateinit var postGroup: PostGroup

    @SpyBean
    lateinit var enrichedPostSerde: EnrichedPostSerde

    @SpyBean
    lateinit var activitySerde: ActivitySerde

    lateinit var topologyTestDriver: TopologyTestDriver
    lateinit var postUserJoinedTopic: TestInputTopic<EnrichedPostKey, EnrichedPostMessage>
    lateinit var enrichedCommentTopic: TestInputTopic<EnrichedCommentKey, EnrichedCommentMessage>
    lateinit var aggregatedActivityTopic: TestInputTopic<ActivityKey, AggregatedActivityMessage>

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
        val aggregatedActivityKStream: AggregatedActivityKStream = streamsBuilder.stream(
            KafkaTopic.ACTIVITY_AGGREGATED, Consumed.with(
                activitySerde.activityKeySerde(), activitySerde.aggregatedActivityMessageSerde()
            )
        )

        val postJoinedKStream: EnrichedPostKStream =
            postJoinerFunction.joinPost().apply(enrichedCommentKStream).apply(aggregatedActivityKStream)
                .apply(postUserJoinedKStream)
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
        aggregatedActivityTopic = topologyTestDriver.createInputTopic(
            KafkaTopic.ACTIVITY_AGGREGATED,
            activitySerde.activityKeySerde().serializer(),
            activitySerde.aggregatedActivityMessageSerde().serializer()
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

        aggregatedActivityTopic.pipeInput(
            TestRecord(createActivityKey(), createAggregatedActivityMessage(1, 1, 3, 3.0f))
        )

        aggregatedActivityTopic.pipeInput(
            TestRecord(createActivityKey(), createAggregatedActivityMessage(2, 2, 4, 1.5f))
        )

        assertFalse(enrichedPostTopic.isEmpty)

        val regularPostMessage: KeyValue<EnrichedPostKey, EnrichedPostMessage> = KeyValue.pair(
            createEnrichedPostKey(), createEnrichedPostMessage(emptyList())
        )

        val postWithFirstCommentMessage = KeyValue.pair(
            createEnrichedPostKey(),
            createEnrichedPostMessage(listOf(createEnrichedCommentMessage("comment 1", "commentId1")))
        )

        val postWithSecondCommentMessage: KeyValue<EnrichedPostKey, EnrichedPostMessage> = KeyValue.pair(
            createEnrichedPostKey(), createEnrichedPostMessage(
                listOf(
                    createEnrichedCommentMessage("comment 1", "commentId1"),
                    createEnrichedCommentMessage("comment 2", "commentId2")
                )
            )
        )

        val postWithFirstActivityMessage: KeyValue<EnrichedPostKey, EnrichedPostMessage> = KeyValue.pair(
            createEnrichedPostKey(), createEnrichedPostMessage(
                listOf(
                    createEnrichedCommentMessage("comment 1", "commentId1"),
                    createEnrichedCommentMessage("comment 2", "commentId2")
                ), 1, 1, 3, 3.0f
            )
        )

        val postWithSecondActivityMessage: KeyValue<EnrichedPostKey, EnrichedPostMessage> = KeyValue.pair(
            createEnrichedPostKey(), createEnrichedPostMessage(
                listOf(
                    createEnrichedCommentMessage("comment 1", "commentId1"),
                    createEnrichedCommentMessage("comment 2", "commentId2")
                ), 2, 2, 4, 1.5f
            )
        )

        val actual: MutableList<KeyValue<EnrichedPostKey, EnrichedPostMessage>> =
            enrichedPostTopic.readKeyValuesToList()

        val expected: List<KeyValue<EnrichedPostKey, EnrichedPostMessage>> = listOf(
            regularPostMessage,
            postWithFirstCommentMessage,
            postWithSecondCommentMessage,
            postWithFirstActivityMessage,
            postWithSecondActivityMessage
        )

        assertNotNull(actual)
        assertEquals(5, actual.size)
        assertResults(expected, actual)
    }

    private fun assertResults(
        expected: List<KeyValue<EnrichedPostKey, EnrichedPostMessage>>,
        actual: List<KeyValue<EnrichedPostKey, EnrichedPostMessage>>
    ) {
        for (i in expected.indices) {
            assertEquals(expected[i], actual[i])
        }
    }

    private fun createActivityKey(): ActivityKey {
        return ActivityKey.newBuilder().build()
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
        comments: List<EnrichedCommentMessage>, views: Long = 0, like: Int = 0, dislike: Int = 0, rating: Float = 0.0f
    ): EnrichedPostMessage {
        val builder: EnrichedPostMessage.Builder =
            EnrichedPostMessage.newBuilder().setId("postId").setTitle("title").setContent("content")
                .setCreatorId("userId").setUser(createUserMessage()).setViews(views).setLikes(like).setDislikes(dislike)
                .setRating(rating)
        builder.addAllComments(comments)

        return builder.build()
    }

    private fun createUserMessage(): UserMessage {
        return UserMessage.newBuilder().setId("userId").setName("name").build()
    }

    private fun createAggregatedActivityMessage(
        views: Long, like: Int, dislike: Int, rating: Float
    ): AggregatedActivityMessage {
        return AggregatedActivityMessage.newBuilder().setPostId("postId").setViews(views).setLikes(like)
            .setDislikes(dislike).setRate(rating).build()
    }
}