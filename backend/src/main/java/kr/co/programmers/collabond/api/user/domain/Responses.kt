package kr.co.programmers.collabond.api.user.domain

import java.time.LocalDateTime

data class UserResponseDto(
    val id: Long,
    val email: String,
    val nickname: String,
    val role: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class SignUpResponseDto(
    val nickname: String,
    val role: String,
    val accessToken: String,
)