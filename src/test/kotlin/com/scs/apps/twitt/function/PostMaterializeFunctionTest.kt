package com.scs.apps.twitt.function

import com.scs.apps.twitt.EnrichedPostKey
import com.scs.apps.twitt.EnrichedPostMessage
import com.scs.apps.twitt.constant.EnrichedPostKStream
import com.scs.apps.twitt.constant.KafkaTopic
import com.scs.apps.twitt.serde.EnrichedPostSerde
import com.scs.apps.twitt.service.PostEventService
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.TestInputTopic
import org.apache.kafka.streams.TopologyTestDriver
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.test.TestRecord
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean

@SpringBootTest(classes = [PostMaterializeFunction::class])
class PostMaterializeFunctionTest {

    @Autowired
    lateinit var postMaterializeFunction: PostMaterializeFunction

    @MockBean
    lateinit var postEventService: PostEventService

    lateinit var topologyTestDriver: TopologyTestDriver
    lateinit var enrichedPostTopic: TestInputTopic<EnrichedPostKey, EnrichedPostMessage>

    @BeforeEach
    fun setup() {
        val streamsBuilder = StreamsBuilder()
        val enrichedPostSerde: EnrichedPostSerde = Mockito.spy(EnrichedPostSerde::class.java)

        val streamInput: EnrichedPostKStream = streamsBuilder.stream(
            KafkaTopic.POST_JOINED,
            Consumed.with(enrichedPostSerde.enrichedPostKeySerde(), enrichedPostSerde.enrichedPostMessageSerde())
        )

        postMaterializeFunction.materializeEnrichedPost().accept(streamInput)

        topologyTestDriver = TopologyTestDriver(streamsBuilder.build())
        enrichedPostTopic = topologyTestDriver.createInputTopic(
            KafkaTopic.POST_JOINED,
            enrichedPostSerde.enrichedPostKeySerde().serializer(),
            enrichedPostSerde.enrichedPostMessageSerde().serializer()
        )
    }

    @AfterEach
    fun cleanup() {
        topologyTestDriver.close()
    }

    @Test
    fun testMaterializeEnrichedPost() {
        val message: EnrichedPostMessage =
            EnrichedPostMessage.newBuilder().setId("id").setTitle("title").setContent("content").build()
        enrichedPostTopic.pipeInput(TestRecord(EnrichedPostKey.getDefaultInstance(), message))

        verify(postEventService).upsert(eq(message))
    }
}