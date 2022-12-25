package com.scs.apps.twitt.controller

import com.scs.apps.twitt.constant.HeaderConstant
import com.scs.apps.twitt.dto.RequestDto
import com.scs.apps.twitt.service.PostService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController

@RestController
class PostController(private val postService: PostService) {

    @PostMapping("post")
    fun createPost(
        @RequestBody createPostRequestDto: RequestDto.CreatePostRequestDto,
        @RequestHeader(HeaderConstant.HEADER_USER_ID) userId: String
    ) {
        postService.createPost(createPostRequestDto, userId)
    }
}