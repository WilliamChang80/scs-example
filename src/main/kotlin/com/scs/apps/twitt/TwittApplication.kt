package com.scs.apps.twitt

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class TwittApplication

fun main(args: Array<String>) {
	runApplication<TwittApplication>(*args)
}
