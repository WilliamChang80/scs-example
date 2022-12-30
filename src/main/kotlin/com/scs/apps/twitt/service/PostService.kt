package com.scs.apps.twitt.service

import com.scs.apps.twitt.dto.RequestDto
import com.scs.apps.twitt.entity.Post
import java.util.UUID

interface PostService {

    fun createPost(createPostRequestDto: RequestDto.CreatePostRequestDto, userId: String)

    fun getPostById(id: UUID): Post
}