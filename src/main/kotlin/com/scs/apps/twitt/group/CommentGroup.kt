package com.scs.apps.twitt.group

import com.scs.apps.twitt.*
import com.scs.apps.twitt.constant.CommentKStream
import com.scs.apps.twitt.constant.EnrichedCommentKStream
import com.scs.apps.twitt.serde.EnrichedCommentSerde
import com.scs.apps.twitt.serde.EnrichedPostSerde
import org.apache.kafka.streams.kstream.Grouped
import org.apache.kafka.streams.kstream.KGroupedStream
import org.apache.kafka.streams.kstream.KeyValueMapper
import org.springframework.stereotype.Component

@Component
class CommentGroup(
    private val enrichedPostSerde: EnrichedPostSerde,
    private val enrichedCommentSerde: EnrichedCommentSerde
) {

    fun group(enrichedCommentKStream: EnrichedCommentKStream): KGroupedStream<EnrichedPostKey, EnrichedCommentMessage> {
        return enrichedCommentKStream.groupBy(
            { _, v: EnrichedCommentMessage -> keyValueMapper(v) },
            groupedSerde()
        )
    }

    private fun keyValueMapper(value: EnrichedCommentMessage): EnrichedPostKey {
        return EnrichedPostKey.newBuilder().setId(value.postId).build()
    }

    private fun groupedSerde(): Grouped<EnrichedPostKey, EnrichedCommentMessage> {
        return Grouped.with(
            "enriched-comment-group-by",
            enrichedPostSerde.enrichedPostKeySerde(),
            enrichedCommentSerde.enrichedCommentMessageSerde()
        )
    }

    fun cogroupAggregator(
        enrichedPostKey: EnrichedPostKey, enrichedCommentMessage: EnrichedCommentMessage,
        enrichedPostMessage: EnrichedPostMessage
    ): EnrichedPostMessage {
        return enrichedPostMessage.toBuilder().addComments(enrichedCommentMessage).build()
    }

}