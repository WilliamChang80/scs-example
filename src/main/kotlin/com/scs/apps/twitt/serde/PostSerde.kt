package com.scs.apps.twitt.serde

import com.scs.apps.twitt.PostKey
import com.scs.apps.twitt.PostMessage
import org.apache.kafka.common.serialization.Serde
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class PostSerde {

    @Bean
    fun postMessageSerde(): Serde<PostMessage> = ProtobufSerde(PostMessage.parser())

    @Bean
    fun postKeySerde(): Serde<PostKey> = ProtobufSerde(PostKey.parser())
}