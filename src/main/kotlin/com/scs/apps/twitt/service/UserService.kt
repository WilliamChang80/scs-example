package com.scs.apps.twitt.service

import com.scs.apps.twitt.dto.RequestDto


interface UserService {

    fun updateUser(updateUserRequestDto: RequestDto.UpdateUserRequestDto, userId: String)
}