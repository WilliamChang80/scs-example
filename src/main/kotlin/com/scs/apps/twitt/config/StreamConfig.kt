package com.scs.apps.twitt.config

import lombok.AllArgsConstructor
import mu.KotlinLogging
import org.apache.kafka.streams.errors.StreamsUncaughtExceptionHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.StreamsBuilderFactoryBean
import org.springframework.kafka.config.StreamsBuilderFactoryBeanConfigurer

@Configuration
@AllArgsConstructor
class StreamConfig {

    private val logger = KotlinLogging.logger {}

    @Bean
    fun streamsCustomizer(): StreamsBuilderFactoryBeanConfigurer {
        return StreamsBuilderFactoryBeanConfigurer { factoryBean: StreamsBuilderFactoryBean ->
            run {
                factoryBean.setStreamsUncaughtExceptionHandler(getStreamsUncaughtExceptionHandler())
            }
        }
    }

    private fun getStreamsUncaughtExceptionHandler(): StreamsUncaughtExceptionHandler {
        return StreamsUncaughtExceptionHandler { e: Throwable ->
            run {
                logger.error(e.message)
                StreamsUncaughtExceptionHandler.StreamThreadExceptionResponse.SHUTDOWN_APPLICATION
            }
        }
    }
}