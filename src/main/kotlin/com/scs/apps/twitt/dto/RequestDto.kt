package com.scs.apps.twitt.dto

import java.util.UUID

object RequestDto {
    data class CreateCommentRequestDto(val comment: String, val postId: String)
    data class UpdateUserRequestDto(val name: String)
    data class CreatePostRequestDto(val title: String, val content: String)

    data class RatePostRequestDto(val id: UUID, val type: String)
}
