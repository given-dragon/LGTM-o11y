package com.caro.member.internal.web

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class CreateMemberRequest(
    @field:Email
    @field:NotBlank
    val email: String,
    
    @field:NotBlank
    val nickname: String
)

data class UpdateNicknameRequest(
    @field:NotBlank
    val nickname: String
)
