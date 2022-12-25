package com.scs.apps.twitt.function

import com.scs.apps.twitt.constant.EnrichedCommentKStream
import com.scs.apps.twitt.constant.PostKStream
import mu.KotlinLogging
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.function.Function

@Configuration
class CommentMaterializeFunction {

    private val logger = KotlinLogging.logger {}

    @Bean
    fun materializeEnrichedComment(): Function<PostKStream, EnrichedCommentKStream> {
        TODO("Implement later")
    }

}