package kr.co.programmers.collabond.api.user.domain

import java.time.LocalDateTime

data class UserResponseDto(
    val id: Long? = null,
    val email: String? = null,
    val nickname: String? = null,
    val role: String? = null,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
)