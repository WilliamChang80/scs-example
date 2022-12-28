package com.scs.apps.twitt

import com.scs.apps.twitt.annotation.EsRepository
import com.scs.apps.twitt.annotation.PostgresRepository
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication
@EnableJpaRepositories(
    includeFilters =
    [ComponentScan.Filter(type = FilterType.ANNOTATION, value = [PostgresRepository::class])]
)
@EnableElasticsearchRepositories(
    includeFilters =
    [ComponentScan.Filter(type = FilterType.ANNOTATION, value = [EsRepository::class])]
)
class TwittApplication

fun main(args: Array<String>) {
    runApplication<TwittApplication>(*args)
}
