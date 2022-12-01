package com.scs.apps.twitt.config

import lombok.AllArgsConstructor
import org.apache.kafka.streams.errors.StreamsUncaughtExceptionHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.StreamsBuilderFactoryBean
import org.springframework.kafka.config.StreamsBuilderFactoryBeanConfigurer

@Configuration
@AllArgsConstructor
class StreamConfig {

    @Bean
    fun streamsCustomizer(): StreamsBuilderFactoryBeanConfigurer {
        return StreamsBuilderFactoryBeanConfigurer {
            factoryBean: StreamsBuilderFactoryBean ->
            run {
                factoryBean.setUncaughtExceptionHandler(getStreamsUncaughtExceptionHandler())
            }
        }
    }

    private fun getStreamsUncaughtExceptionHandler(): Thread.UncaughtExceptionHandler {
        return Thread.UncaughtExceptionHandler  {
                _: Thread, e: Throwable ->
            run {
                println(e.message)
                StreamsUncaughtExceptionHandler.StreamThreadExceptionResponse.SHUTDOWN_APPLICATION;
            }
        }
    }
}