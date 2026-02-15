package com.caro.member

import java.time.Instant

data class MemberDto(
    val id: Long,
    val email: String,
    val nickname: String,
    val createdAt: Instant
)
