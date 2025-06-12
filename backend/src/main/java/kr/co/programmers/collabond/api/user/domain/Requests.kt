package kr.co.programmers.collabond.api.user.domain

data class UserSignUpRequestDto(
    val nickname: String,
    val role: String
)

data class UserUpdateRequestDto(
    val nickname: String
)