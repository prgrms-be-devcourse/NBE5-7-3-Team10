package kr.co.programmers.collabond.api.user.interfaces

import kr.co.programmers.collabond.api.user.domain.SignUpResponseDto
import kr.co.programmers.collabond.api.user.domain.User
import kr.co.programmers.collabond.api.user.domain.UserResponseDto

fun toResponseDto(user: User) = UserResponseDto(
    id = user.id,
    email = user.email,
    nickname = user.nickname,
    role = user.role.name,
    createdAt = user.createdAt,
    updatedAt = user.updatedAt
)

fun toSignUpResponseDto(user: User, accessToken: String) = SignUpResponseDto(
    nickname = user.nickname,
    role = user.role.name,
    accessToken = accessToken
)