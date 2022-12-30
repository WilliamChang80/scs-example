package com.scs.apps.twitt.group

import org.apache.kafka.streams.kstream.KGroupedStream
import org.apache.kafka.streams.kstream.KStream

interface BaseGroup<K, V, VR> {

    fun <VL : KStream<Any, Any>> group(stream: VL): KGroupedStream<K, V>
    fun cogroupAggregator(value: V, aggregate: VR): VR
}