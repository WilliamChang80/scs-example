package com.scs.apps.twitt.serde

import com.scs.apps.twitt.EnrichedCommentKey
import com.scs.apps.twitt.EnrichedCommentMessage
import org.apache.kafka.common.serialization.Serde
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class EnrichedCommentSerde {

    @Bean
    fun enrichedCommentKeySerde(): Serde<EnrichedCommentKey> = ProtobufSerde(EnrichedCommentKey.parser())

    @Bean
    fun enrichedCommentMessageSerde(): Serde<EnrichedCommentMessage> = ProtobufSerde(EnrichedCommentMessage.parser())
}