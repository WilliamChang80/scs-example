package com.scs.apps.twitt.function

import com.scs.apps.twitt.constant.PostKStream
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.function.Consumer

@Configuration
class PostJoinerFunction {

    @Bean
    fun joinPost(): Consumer<PostKStream> {
        return Consumer { func ->
            func.peek { key, value -> println("Received PostMessage with key: $key and message: $value") }
        }
    }

}