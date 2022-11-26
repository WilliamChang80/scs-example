package com.scs.apps.twitt.config

import lombok.AllArgsConstructor
import org.apache.kafka.streams.errors.StreamsUncaughtExceptionHandler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringApplication
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
                factoryBean.setStreamsUncaughtExceptionHandler(getStreamsUncaughtExceptionHandler())
            }
        }
    }

    private fun getStreamsUncaughtExceptionHandler(): StreamsUncaughtExceptionHandler {
        return StreamsUncaughtExceptionHandler {
            StreamsUncaughtExceptionHandler.StreamThreadExceptionResponse.SHUTDOWN_APPLICATION;
        }
    }
}