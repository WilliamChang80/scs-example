package com.scs.apps.twitt.group

import com.scs.apps.twitt.EnrichedPostKey
import com.scs.apps.twitt.EnrichedPostMessage
import com.scs.apps.twitt.constant.EnrichedPostKStream
import com.scs.apps.twitt.serde.EnrichedPostSerde
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.mock.mockito.SpyBean

@SpringBootTest(classes = [PostGroup::class])
class PostGroupTest {

    @Autowired
    lateinit var postGroup: PostGroup

    @SpyBean
    lateinit var enrichedPostSerde: EnrichedPostSerde

    @MockBean
    lateinit var enrichedPostKStream: EnrichedPostKStream

    @Test
    fun testCogroupAggregator() {
        val actual: EnrichedPostMessage = postGroup.cogroupAggregator(
            EnrichedPostKey.getDefaultInstance(), EnrichedPostMessage.getDefaultInstance(),
            EnrichedPostMessage.getDefaultInstance()
        )

        assertEquals(EnrichedPostMessage.getDefaultInstance(), actual)
    }
}