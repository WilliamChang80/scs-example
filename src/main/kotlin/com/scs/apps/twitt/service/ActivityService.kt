package com.scs.apps.twitt.service

import com.scs.apps.twitt.dto.RequestDto

interface ActivityService {

    fun ratePost(requestDto: RequestDto.RatePostRequestDto)
}