package com.scs.apps.twitt.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class HomeController {

    @GetMapping("/{str}")
    fun init(@PathVariable str: String): String = "$str hello"
}