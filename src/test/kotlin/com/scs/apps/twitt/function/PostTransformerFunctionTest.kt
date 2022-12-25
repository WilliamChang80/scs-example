package com.scs.apps.twitt.function

import com.scs.apps.twitt.PostCdcKey
import com.scs.apps.twitt.PostCdcMessage
import com.scs.apps.twitt.PostKey
import com.scs.apps.twitt.PostMessage
import com.scs.apps.twitt.constant.KafkaTopic
import com.scs.apps.twitt.constant.PostCdcKStream
import com.scs.apps.twitt.constant.PostKStream
import com.scs.apps.twitt.mapper.PostCdcMapper
import com.scs.apps.twitt.serde.PostCdcSerde
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
import org.mockito.Mockito.spy
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import java.util.*

@SpringBootTest(classes = [PostTransformerFunction::class])
class PostTransformerFunctionTest {

    @Autowired
    lateinit var postTransformerFunction: PostTransformerFunction

    @MockBean
    lateinit var postCdcMapper: PostCdcMapper

    lateinit var topologyTestDriver: TopologyTestDriver
    lateinit var postCdcTopic: TestInputTopic<PostCdcKey, PostCdcMessage>
    lateinit var postTopic: TestOutputTopic<PostKey, PostMessage>

    @BeforeEach
    fun setup() {
        val streamsBuilder = StreamsBuilder()
        val props = Properties()
        val postCdcSerde: PostCdcSerde = spy(PostCdcSerde::class.java)
        val postSerde: PostSerde = spy(PostSerde::class.java)

        val streamInput: PostCdcKStream = streamsBuilder.stream(
            KafkaTopic.POST_CREATED_TOPIC,
            Consumed.with(postCdcSerde.postCdcKeySerde(), postCdcSerde.postCdcMessageSerde())
        )
        val streamOutput: PostKStream = postTransformerFunction.convertPostCdcToPostMessage().apply(streamInput)
        streamOutput.to(
            KafkaTopic.POST_TRANSFORMED_TOPIC, Produced.with(postSerde.postKeySerde(), postSerde.postMessageSerde())
        )

        topologyTestDriver = TopologyTestDriver(streamsBuilder.build(), props)
        postCdcTopic = topologyTestDriver.createInputTopic(
            KafkaTopic.POST_CREATED_TOPIC,
            postCdcSerde.postCdcKeySerde().serializer(),
            postCdcSerde.postCdcMessageSerde().serializer()
        )
        postTopic = topologyTestDriver.createOutputTopic(
            KafkaTopic.POST_TRANSFORMED_TOPIC,
            postSerde.postKeySerde().deserializer(),
            postSerde.postMessageSerde().deserializer()
        )
    }

    @AfterEach
    fun cleanup() {
        topologyTestDriver.close()
    }

    @Test
    fun testConvertPostCdcToPostMessage() {
        `when`(postCdcMapper.apply(createPostCdcKey("1"), createPostCdcMessage("2"))).thenReturn(
                KeyValue.pair(
                    createPostKey("3"),
                    createPostMessage("4")
                )
            )

        postCdcTopic.pipeInput(TestRecord(createPostCdcKey("1"), createPostCdcMessage("2")))

        assertFalse(postTopic.isEmpty)

        val actual: KeyValue<PostKey, PostMessage> = postTopic.readKeyValue()

        assertEquals(createPostKey("3"), actual.key)
        assertEquals(createPostMessage("4"), actual.value)
    }

    private fun createPostCdcKey(id: String): PostCdcKey {
        return PostCdcKey.newBuilder().setId(id).build()
    }

    private fun createPostCdcMessage(id: String): PostCdcMessage {
        return PostCdcMessage.newBuilder().setId(id).build()
    }

    private fun createPostKey(id: String): PostKey {
        return PostKey.newBuilder().setId(id).build()
    }

    private fun createPostMessage(id: String): PostMessage {
        return PostMessage.newBuilder().setId(id).build()
    }

}