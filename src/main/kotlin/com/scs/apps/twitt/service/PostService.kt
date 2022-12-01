package com.scs.apps.twitt.service

import com.scs.apps.twitt.dto.CreatePostRequestDto

interface PostService {

    fun createPost(createPostRequestDto: CreatePostRequestDto, userId: String)
}