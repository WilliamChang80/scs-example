package com.scs.apps.twitt.service

import com.scs.apps.twitt.dto.RequestDto

interface PostService {

    fun createPost(createPostRequestDto: RequestDto.CreatePostRequestDto, userId: String)
}