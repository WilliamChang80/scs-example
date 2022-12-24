package com.scs.apps.twitt.group

import com.scs.apps.twitt.EnrichedPostKey
import com.scs.apps.twitt.EnrichedPostMessage
import com.scs.apps.twitt.constant.EnrichedPostKStream
import com.scs.apps.twitt.serde.EnrichedPostSerde
import org.apache.kafka.streams.kstream.Grouped
import org.apache.kafka.streams.kstream.KGroupedStream
import org.springframework.stereotype.Component

@Component
class PostGroup(private val enrichedPostSerde: EnrichedPostSerde) {

    fun group(enrichedPostKStream: EnrichedPostKStream): KGroupedStream<EnrichedPostKey, EnrichedPostMessage> {
        return enrichedPostKStream.groupBy(
            { _: EnrichedPostKey, v: EnrichedPostMessage -> keyValueMapper(v) },
            groupedSerde()
        )
    }

    private fun keyValueMapper(value: EnrichedPostMessage): EnrichedPostKey {
        return EnrichedPostKey.newBuilder().setId(value.id).build()
    }

    private fun groupedSerde(): Grouped<EnrichedPostKey, EnrichedPostMessage> {
        return Grouped.with("post-group-by", enrichedPostSerde.enrichedPostKeySerde(),
            enrichedPostSerde.enrichedPostMessageSerde())
    }

    fun cogroupAggregator(
        enrichedPostKey: EnrichedPostKey, postMessageWithUser: EnrichedPostMessage, enrichedPostMessage: EnrichedPostMessage
    ): EnrichedPostMessage {
        enrichedPostMessage.toBuilder().setId(postMessageWithUser.id).setContent(postMessageWithUser.content)
            .setCreatedAt(postMessageWithUser.createdAt)
            .setUpdatedAt(postMessageWithUser.updatedAt)
            .setTitle(postMessageWithUser.title)
            .setUser(postMessageWithUser.user)
            .build()

        return enrichedPostMessage
    }
}