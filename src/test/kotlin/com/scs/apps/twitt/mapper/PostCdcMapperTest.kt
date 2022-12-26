package com.scs.apps.twitt.mapper

import com.google.protobuf.Timestamp
import com.scs.apps.twitt.PostCdcKey
import com.scs.apps.twitt.PostCdcMessage
import com.scs.apps.twitt.PostKey
import com.scs.apps.twitt.PostMessage
import org.apache.kafka.streams.KeyValue
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(classes = [PostCdcMapper::class])
class PostCdcMapperTest {

    @Autowired
    lateinit var postCdcMapper: PostCdcMapper

    @Test
    fun testApply() {
        val now: Timestamp = Timestamp.getDefaultInstance()
        val actual: KeyValue<PostKey, PostMessage> = postCdcMapper.apply(
            PostCdcKey.newBuilder().setId("id1").build(), PostCdcMessage.newBuilder().setId("id2")
                .setRating(2.0f)
                .setContent("content")
                .setCreatorId("creatorId")
                .setIsDeleted(false)
                .setCreatedAt(now)
                .setUpdatedAt(now).build()
        )

        val expected: KeyValue<PostKey, PostMessage> = KeyValue.pair(
            PostKey.newBuilder().setId("id1").build(), PostMessage.newBuilder().setId("id2")
                .setRating(2.0f)
                .setContent("content")
                .setCreatorId("creatorId")
                .setIsDeleted(false)
                .setCreatedAt(now)
                .setUpdatedAt(now).build()
        )

        assertEquals(expected, actual)
    }
}