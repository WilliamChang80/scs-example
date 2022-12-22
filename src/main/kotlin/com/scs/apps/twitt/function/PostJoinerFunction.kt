package com.scs.apps.twitt.function

import com.scs.apps.twitt.constant.PostKStream
import mu.KotlinLogging
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.function.Consumer

@Configuration
class PostJoinerFunction {

    private val logger = KotlinLogging.logger {}

    @Bean
    fun joinPost(): Consumer<PostKStream> {
        return Consumer { func ->
            func.peek { key, value -> logger.info("Received PostMessage with key: $key and message: $value") }
        }
    }

}