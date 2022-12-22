package com.scs.apps.twitt.dto

object RequestDto {
    data class CreateCommentRequestDto(val comment: String)
    data class UpdateUserRequestDto(val name: String)
    data class CreatePostRequestDto(val title: String, val content: String)
}
