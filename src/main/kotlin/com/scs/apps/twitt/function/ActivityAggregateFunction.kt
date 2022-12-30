package com.scs.apps.twitt.function

import com.scs.apps.twitt.*
import com.scs.apps.twitt.constant.ActivityKStream
import com.scs.apps.twitt.constant.AggregatedActivityKStream
import com.scs.apps.twitt.serde.ActivitySerde
import com.scs.apps.twitt.serde.PostSerde
import mu.KotlinLogging
import org.apache.kafka.common.utils.Bytes
import org.apache.kafka.streams.KeyValue
import org.apache.kafka.streams.kstream.Grouped
import org.apache.kafka.streams.kstream.Materialized
import org.apache.kafka.streams.kstream.Named
import org.apache.kafka.streams.state.KeyValueStore
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.function.Function

@Configuration
class ActivityAggregateFunction(private val activitySerde: ActivitySerde, private val postSerde: PostSerde) {

    private val logger = KotlinLogging.logger {}

    @Bean
    fun aggregateActivities(): Function<ActivityKStream, AggregatedActivityKStream> {
        return Function { activity: ActivityKStream ->
            activity.peek { _, v -> logger.info("received activity message on post id ${v.postId} and action type ${v.actionType}") }
                .map { _: ActivityKey, v: ActivityMessage ->
                    KeyValue.pair(PostKey.newBuilder().setId(v.postId).build(), v)
                }
                .groupByKey(Grouped.with(postSerde.postKeySerde(), activitySerde.activityMessageSerde()))
                .aggregate({ AggregatedActivityMessage.newBuilder().build() }, { k, v, r -> aggregate(k, v, r) },
                    materializedSerde()
                )
                .toStream(Named.`as`("activity-aggregated"))
                .selectKey { _, value: AggregatedActivityMessage ->
                    ActivityKey.newBuilder().setId(value.id).build()
                }
                .peek { k, v -> logger.info("aggregated post with id ${k.id} and values $v") }
        }
    }

    private fun materializedSerde(): Materialized<PostKey, AggregatedActivityMessage, KeyValueStore<Bytes, ByteArray>> {
        return Materialized.`as`<PostKey, AggregatedActivityMessage, KeyValueStore<Bytes, ByteArray>>("activity-joined-state-store")
            .withKeySerde(postSerde.postKeySerde())
            .withValueSerde(activitySerde.aggregatedActivityMessageSerde())
    }

    private fun aggregate(
        k: PostKey,
        v: ActivityMessage,
        aggregate: AggregatedActivityMessage
    ): AggregatedActivityMessage {
        val builder: AggregatedActivityMessage.Builder = aggregate.toBuilder().setPostId(k.id).setId(v.id)
        when (v.actionType) {
            ActivityActionType.POST_VIEWED -> builder.views++
            ActivityActionType.POST_LIKED -> builder.likes++
            ActivityActionType.POST_DISLIKED -> builder.dislikes++
            else -> {}
        }

        // Counted because storing the latest data, can use count/reduce from stream.
        val rateCounts = aggregate.likes + aggregate.dislikes
        if (rateCounts != 0) {
            builder.rate = (builder.likes.toFloat() / rateCounts.toFloat()) * 10
        }

        return builder.build()
    }
}