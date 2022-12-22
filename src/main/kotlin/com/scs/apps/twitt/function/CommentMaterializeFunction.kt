package com.scs.apps.twitt.function

import com.scs.apps.twitt.constant.EnrichedCommentKStream
import mu.KotlinLogging
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.function.Consumer

@Configuration
class CommentMaterializeFunction {

    private val logger = KotlinLogging.logger {}

    @Bean
    fun materializeEnrichedComment(): Consumer<EnrichedCommentKStream> {
        return Consumer { func ->
            func.peek { key, value -> logger.info("Received EnrichedCommentMessage with key: $key and message: $value") }
        }
    }

}