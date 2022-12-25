package com.scs.apps.twitt.controller

import com.scs.apps.twitt.constant.HeaderConstant
import com.scs.apps.twitt.dto.RequestDto
import com.scs.apps.twitt.service.UserService
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController

@RestController
class UserController(private val userService: UserService) {

    @PutMapping("user")
    fun createPost(
        @RequestBody updateUserRequestDto: RequestDto.UpdateUserRequestDto,
        @RequestHeader(HeaderConstant.HEADER_USER_ID) userId: String
    ) {
        userService.updateUser(updateUserRequestDto, userId)
    }
}