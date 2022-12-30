package com.scs.apps.twitt.group

import com.scs.apps.twitt.AggregatedActivityMessage
import com.scs.apps.twitt.EnrichedPostKey
import com.scs.apps.twitt.EnrichedPostMessage
import com.scs.apps.twitt.constant.AggregatedActivityKStream
import com.scs.apps.twitt.serde.ActivitySerde
import com.scs.apps.twitt.serde.EnrichedPostSerde
import org.apache.kafka.streams.kstream.Grouped
import org.apache.kafka.streams.kstream.KGroupedStream
import org.springframework.stereotype.Component

@Component
class ActivityGroup(
    private val enrichedPostSerde: EnrichedPostSerde,
    private val aggregatedActivitySerde: ActivitySerde
) {

    fun group(activityKStream: AggregatedActivityKStream): KGroupedStream<EnrichedPostKey, AggregatedActivityMessage> {
        return activityKStream.groupBy(
            { _, v: AggregatedActivityMessage -> keyValueMapper(v) },
            groupedSerde()
        )
    }

    private fun keyValueMapper(value: AggregatedActivityMessage): EnrichedPostKey {
        return EnrichedPostKey.newBuilder().setId(value.postId).build()
    }

    private fun groupedSerde(): Grouped<EnrichedPostKey, AggregatedActivityMessage> {
        return Grouped.with(
            "aggregated-activity-group-by", enrichedPostSerde.enrichedPostKeySerde(),
            aggregatedActivitySerde.aggregatedActivityMessageSerde()
        )
    }

    fun cogroupAggregator(
        key: EnrichedPostKey,
        value: AggregatedActivityMessage,
        result: EnrichedPostMessage
    ): EnrichedPostMessage {
        return result.toBuilder().setViews(value.views).setLikes(value.likes)
            .setDislikes(value.dislikes)
            .setRating(value.rate)
            .build()
    }
}