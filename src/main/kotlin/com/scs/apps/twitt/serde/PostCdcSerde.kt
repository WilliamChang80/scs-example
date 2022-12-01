package com.scs.apps.twitt.serde

import com.scs.apps.twitt.PostCdcKey
import com.scs.apps.twitt.PostCdcMessage
import org.apache.kafka.common.serialization.Serde
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class PostCdcSerde {

    @Bean
    fun postCdcKeySerde(): Serde<PostCdcKey> = ProtobufSerde(PostCdcKey.parser())

    @Bean
    fun postCdcMessageSerde(): Serde<PostCdcMessage> = ProtobufSerde(PostCdcMessage.parser())
}