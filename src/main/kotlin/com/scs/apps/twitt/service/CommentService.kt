package com.scs.apps.twitt.service

import com.scs.apps.twitt.dto.RequestDto

interface CommentService {
    fun createComment(createCommentRequestDto: RequestDto.CreateCommentRequestDto, userId: String)
}