package com.scs.apps.twitt.function

import com.scs.apps.twitt.ActivityActionType
import com.scs.apps.twitt.ActivityKey
import com.scs.apps.twitt.ActivityMessage
import com.scs.apps.twitt.AggregatedActivityMessage
import com.scs.apps.twitt.constant.ActivityKStream
import com.scs.apps.twitt.constant.AggregatedActivityKStream
import com.scs.apps.twitt.constant.KafkaTopic
import com.scs.apps.twitt.serde.ActivitySerde
import com.scs.apps.twitt.serde.PostSerde
import org.apache.kafka.streams.*
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.kstream.Produced
import org.apache.kafka.streams.test.TestRecord
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.SpyBean
import java.util.*

@SpringBootTest(classes = [ActivityAggregateFunction::class])
class ActivityAggregateFunctionTest {

    @SpyBean
    lateinit var activitySerde: ActivitySerde

    @SpyBean
    lateinit var postSerde: PostSerde

    @Autowired
    lateinit var activityAggregateFunction: ActivityAggregateFunction

    lateinit var topologyTestDriver: TopologyTestDriver
    lateinit var activityTopic: TestInputTopic<ActivityKey, ActivityMessage>

    lateinit var aggregatedActivityTopic: TestOutputTopic<ActivityKey, AggregatedActivityMessage>

    @BeforeEach
    fun setup() {
        val streamsBuilder = StreamsBuilder()

        val activityKStream: ActivityKStream = streamsBuilder.stream(
            KafkaTopic.ACTIVITY_UPDATED, Consumed.with(
                activitySerde.activityKeySerde(), activitySerde.activityMessageSerde()
            )
        )

        val aggregatedActivityKStream: AggregatedActivityKStream = activityAggregateFunction
            .aggregateActivities().apply(activityKStream)
        aggregatedActivityKStream.to(
            KafkaTopic.ACTIVITY_AGGREGATED, Produced.with(
                activitySerde.activityKeySerde(), activitySerde.aggregatedActivityMessageSerde()
            )
        )

        val props = Properties()
        props[StreamsConfig.APPLICATION_ID_CONFIG] = "TopologyTestDriver"
        props[StreamsConfig.BOOTSTRAP_SERVERS_CONFIG] = "ignored"

        topologyTestDriver = TopologyTestDriver(streamsBuilder.build(), props)
        activityTopic = topologyTestDriver.createInputTopic(
            KafkaTopic.ACTIVITY_UPDATED,
            activitySerde.activityKeySerde().serializer(), activitySerde.activityMessageSerde().serializer()
        )

        aggregatedActivityTopic = topologyTestDriver.createOutputTopic(
            KafkaTopic.ACTIVITY_AGGREGATED,
            activitySerde.activityKeySerde().deserializer(),
            activitySerde.aggregatedActivityMessageSerde().deserializer()
        )
    }

    @AfterEach
    fun cleanup() {
        topologyTestDriver.close()
    }

    @Test
    fun testAggregateActivities() {
        val numberOfTest = 2
        for (i in 0..numberOfTest) {
            activityTopic.pipeInput(
                TestRecord(
                    ActivityKey.newBuilder().build(),
                    createActivityMessage(ActivityActionType.POST_VIEWED)
                )
            )
        }

        activityTopic.pipeInput(
            TestRecord(
                ActivityKey.newBuilder().build(),
                createActivityMessage(ActivityActionType.POST_LIKED)
            )
        )

        activityTopic.pipeInput(
            TestRecord(
                ActivityKey.newBuilder().build(),
                createActivityMessage(ActivityActionType.POST_LIKED)
            )
        )

        activityTopic.pipeInput(
            TestRecord(
                ActivityKey.newBuilder().build(),
                createActivityMessage(ActivityActionType.POST_DISLIKED)
            )
        )

        assertFalse(aggregatedActivityTopic.isEmpty)
        val actual: MutableList<KeyValue<ActivityKey, AggregatedActivityMessage>> =
            aggregatedActivityTopic.readKeyValuesToList()
        val expected: List<AggregatedActivityMessage> = listOf(
            createAggregatedActivityMessage(views = 1),
            createAggregatedActivityMessage(views = 2),
            createAggregatedActivityMessage(views = 3),
            createAggregatedActivityMessage(views = 3, likes = 1, rating = 10f),
            createAggregatedActivityMessage(views = 3, likes = 2, rating = 10f),
            createAggregatedActivityMessage(views = 3, likes = 2, dislikes = 1, rating = 6.666667f)
        )

        assertEquals(expected.size, actual.size)

        for (i in expected.indices) {
            assertEquals(expected[i], actual[i].value)
        }
    }

    private fun createAggregatedActivityMessage(
        likes: Int = 0,
        dislikes: Int = 0,
        rating: Float = 0.0f,
        views: Long = 0
    )
            : AggregatedActivityMessage {
        return AggregatedActivityMessage.newBuilder()
            .setPostId("8c75ed6a-5647-46d8-8bb0-106cf5e455ef")
            .setLikes(likes)
            .setDislikes(dislikes)
            .setRate(rating)
            .setViews(views)
            .build()
    }

    private fun createActivityMessage(actionType: ActivityActionType): ActivityMessage {
        return ActivityMessage.newBuilder()
            .setPostId("8c75ed6a-5647-46d8-8bb0-106cf5e455ef")
            .setActionType(actionType)
            .build()
    }
}