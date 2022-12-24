package com.scs.apps.twitt.function

import com.scs.apps.twitt.constant.EnrichedPostKStream
import mu.KotlinLogging
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.function.Consumer

@Configuration
class PostMaterializeFunction {

    private val logger = KotlinLogging.logger {}

    @Bean
    fun materializeEnrichedPost(): Consumer<EnrichedPostKStream> {
        return Consumer { post: EnrichedPostKStream ->
            post.peek { key, value ->
                logger.info("materializing post with key $key and value $value")
            }
        }
    }
}