package com.scs.apps.twitt.function

import com.scs.apps.twitt.PostKey
import com.scs.apps.twitt.PostMessage
import org.apache.kafka.streams.KeyValue
import org.apache.kafka.streams.kstream.KStream
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.function.Consumer

@Configuration
class PostJoinerFunction {

    @Bean
    fun test(): Consumer<KStream<ByteArray, ByteArray>> {
        return Consumer { func ->
            func
                .map { k, v -> convert(k, v) }
                .peek { key, value -> println("Received PostMessage with key: $key and message: $value") }
        }
    }

    private fun convert(k: ByteArray, v: ByteArray): KeyValue<PostKey, PostMessage>? {
        try {
            return KeyValue.pair(PostKey.parseFrom(k), PostMessage.parseFrom(v))
        } catch (e: Exception) {
            println("exception occurred $e")
        }

        return null
    }

}