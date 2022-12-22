package com.scs.apps.twitt.controller

import com.scs.apps.twitt.constant.HeaderConstant
import com.scs.apps.twitt.dto.RequestDto
import com.scs.apps.twitt.service.CommentService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController

@RestController
class CommentController(private val commentService: CommentService) {

    @PostMapping("comment")
    fun createComment(
        @RequestBody createCommentRequestDto: RequestDto.CreateCommentRequestDto,
        @RequestHeader(HeaderConstant.HEADER_USER_ID) userId: String
    ) {
        commentService.createComment(createCommentRequestDto, userId)
    }
}