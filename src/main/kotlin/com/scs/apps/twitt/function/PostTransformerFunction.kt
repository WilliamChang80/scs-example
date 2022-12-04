package com.scs.apps.twitt.function

import com.scs.apps.twitt.PostCdcKey
import com.scs.apps.twitt.PostCdcMessage
import com.scs.apps.twitt.PostKey
import com.scs.apps.twitt.PostMessage
import com.scs.apps.twitt.mapper.PostCdcMapper
import org.apache.kafka.streams.kstream.KStream
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.function.Function

@Configuration
class PostTransformerFunction(private val postCdcMapper: PostCdcMapper) {

    @Bean
    fun convertPostCdcToPostMessage(): Function<KStream<PostCdcKey, PostCdcMessage>, KStream<PostKey, PostMessage>> {
        return Function { func ->
            func.peek { key, value -> println("Received PostCdcMessage with key: $key and message: $value") }
                .map { k: PostCdcKey, v: PostCdcMessage -> postCdcMapper.apply(k, v) }
        }
    }
}