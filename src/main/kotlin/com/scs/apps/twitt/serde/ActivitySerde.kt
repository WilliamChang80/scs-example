package com.scs.apps.twitt.serde

import com.scs.apps.twitt.ActivityKey
import com.scs.apps.twitt.ActivityMessage
import com.scs.apps.twitt.AggregatedActivityMessage
import org.apache.kafka.common.serialization.Serde
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ActivitySerde {

    @Bean
    fun activityKeySerde(): Serde<ActivityKey> = ProtobufSerde(ActivityKey.parser())

    @Bean
    fun activityMessageSerde(): Serde<ActivityMessage> = ProtobufSerde(ActivityMessage.parser())

    @Bean
    fun aggregatedActivityMessageSerde(): Serde<AggregatedActivityMessage> = ProtobufSerde(AggregatedActivityMessage.parser())
}