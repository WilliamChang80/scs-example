package com.scs.apps.twitt.serde

import com.scs.apps.twitt.Comment.CommentKey
import com.scs.apps.twitt.Comment.CommentMessage
import org.apache.kafka.common.serialization.Serde
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class CommentSerde {

    @Bean
    fun commentKeySerde(): Serde<CommentKey> = ProtobufSerde(CommentKey.parser())

    @Bean
    fun commentMessageSerde(): Serde<CommentMessage> = ProtobufSerde(CommentMessage.parser())
}