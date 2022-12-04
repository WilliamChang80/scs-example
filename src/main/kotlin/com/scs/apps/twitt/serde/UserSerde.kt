package com.scs.apps.twitt.serde

import com.scs.apps.twitt.UserKey
import com.scs.apps.twitt.UserMessage
import org.apache.kafka.common.serialization.Serde
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class UserSerde {

    @Bean
    fun userKeySerde(): Serde<UserKey> = ProtobufSerde(UserKey.parser())

    @Bean
    fun userMessageSerde(): Serde<UserMessage> = ProtobufSerde(UserMessage.parser())
}