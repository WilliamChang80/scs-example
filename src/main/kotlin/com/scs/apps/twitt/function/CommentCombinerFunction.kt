package com.scs.apps.twitt.function

import com.scs.apps.twitt.Comment.CommentKey
import com.scs.apps.twitt.Comment.CommentMessage
import com.scs.apps.twitt.EnrichedCommentKey
import com.scs.apps.twitt.EnrichedCommentMessage
import com.scs.apps.twitt.UserKey
import com.scs.apps.twitt.UserMessage
import lombok.extern.slf4j.Slf4j
import org.apache.kafka.streams.kstream.KStream
import org.apache.kafka.streams.kstream.KTable
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.function.BiFunction

@Configuration
@Slf4j
class CommentCombinerFunction {

    @Bean
    fun commentUserCombiner(): BiFunction<KTable<UserKey, UserMessage>,
            KTable<CommentKey, CommentMessage>,
            KStream<EnrichedCommentKey, EnrichedCommentMessage>> {
        throw NotImplementedError()
    }
}