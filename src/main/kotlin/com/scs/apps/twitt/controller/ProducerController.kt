package com.scs.apps.twitt.controller

import com.scs.apps.twitt.dto.CreatePostRequestDto
import com.scs.apps.twitt.service.PostService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController

@RestController
class ProducerController(private val postService: PostService) {

    @PostMapping("/produce")
    fun createPost(@RequestBody createPostRequestDto: CreatePostRequestDto,
                   @RequestHeader("X-User-ID") userId: String) {
        postService.createPost(createPostRequestDto, userId)
    }
}