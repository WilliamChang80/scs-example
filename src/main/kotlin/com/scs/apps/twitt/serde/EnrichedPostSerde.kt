package com.scs.apps.twitt.serde

import com.scs.apps.twitt.EnrichedPostKey
import com.scs.apps.twitt.EnrichedPostMessage
import org.apache.kafka.common.serialization.Serde
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class EnrichedPostSerde {

    @Bean
    fun enrichedPostKeySerde(): Serde<EnrichedPostKey> = ProtobufSerde(EnrichedPostKey.parser())

    @Bean
    fun enrichedPostMessageSerde(): Serde<EnrichedPostMessage> = ProtobufSerde(EnrichedPostMessage.parser())
}