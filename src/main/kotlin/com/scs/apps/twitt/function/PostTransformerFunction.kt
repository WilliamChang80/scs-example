package com.scs.apps.twitt.function

import com.scs.apps.twitt.PostCdcKey
import com.scs.apps.twitt.PostCdcMessage
import com.scs.apps.twitt.constant.PostCdcKStream
import com.scs.apps.twitt.constant.PostKStream
import com.scs.apps.twitt.mapper.PostCdcMapper
import mu.KotlinLogging
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.function.Function

@Configuration
class PostTransformerFunction(private val postCdcMapper: PostCdcMapper) {
    private val logger = KotlinLogging.logger {}

    @Bean
    fun convertPostCdcToPostMessage(): Function<PostCdcKStream, PostKStream> {
        return Function { func ->
            func.peek { key, value -> logger.info("Received PostCdcMessage with key: $key and message: $value") }
                .map { k: PostCdcKey, v: PostCdcMessage -> postCdcMapper.apply(k, v) }
        }
    }
}