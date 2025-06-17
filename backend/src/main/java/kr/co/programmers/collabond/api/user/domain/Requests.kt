package kr.co.programmers.collabond.api.user.domain

import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class UserSignUpRequestDto(
    @field:Size(max = 20, message = "닉네임은 20자 이하여야 합니다.")
    @field:Pattern(
        regexp = "^[가-힣a-zA-Z0-9]+$",
        message = "닉네임은 필수 항목이고, 한글, 영문, 숫자만 가능합니다"
    )
    val nickname: String,

    val role: Role
)

data class UserUpdateRequestDto(
    @field:Size(max = 20, message = "닉네임은 20자 이하여야 합니다.")
    @field:Pattern(
        regexp = "^[가-힣a-zA-Z0-9]+$",
        message = "닉네임은 필수 항목이고, 한글, 영문, 숫자만 가능합니다"
    )
    val nickname: String
)