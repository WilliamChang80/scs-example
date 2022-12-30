package com.scs.apps.twitt.controller

import com.scs.apps.twitt.dto.RequestDto
import com.scs.apps.twitt.service.ActivityService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
class ActivityController(private val activityService: ActivityService) {

    @PostMapping("/posts/rate")
    fun ratePost(
        @RequestBody requestDto: RequestDto.RatePostRequestDto
    ) {
        return activityService.ratePost(requestDto)
    }
}