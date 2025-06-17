package kr.co.programmers.collabond.core.auth.jwt

import kr.co.programmers.collabond.api.user.domain.Role

data class AccessTokenResponseDto(
    val accessToken: String
)

data class TokenBodyDto(
    val providerId: String,
    val role: Role
)

data class LoginTokenResponseDto(
    val accessToken: String,
    val refreshToken: String,
    val id: Long,
    val nickname: String,
    val role: Role
)