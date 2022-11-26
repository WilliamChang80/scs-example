package com.scs.apps.twitt.serde

import com.scs.apps.twitt.PostMessage
import org.apache.kafka.common.serialization.Serde
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class PostSerde {

    @Bean
    fun postSerializer(): Serde<PostMessage> = ProtobufSerde(PostMessage.parser())

}