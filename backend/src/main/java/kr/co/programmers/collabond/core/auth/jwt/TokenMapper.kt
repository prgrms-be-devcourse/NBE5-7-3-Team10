package kr.co.programmers.collabond.core.auth.jwt

import kr.co.programmers.collabond.api.user.domain.Role
import kr.co.programmers.collabond.api.user.domain.User

fun toTokenBodyDto(sub: String, role: String): TokenBodyDto =
    TokenBodyDto(
        providerId = sub,
        role = Role.valueOf(role.uppercase())
    )

fun toLoginTokenResponseDto(
    accessToken: String,
    refreshToken: String,
    user: User
): LoginTokenResponseDto =
    LoginTokenResponseDto(
        accessToken = accessToken,
        refreshToken = refreshToken,
        id = user.id,
        nickname = user.nickname,
        role = user.role
    )

