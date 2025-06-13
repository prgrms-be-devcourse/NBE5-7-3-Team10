package kr.co.programmers.collabond.api.apply.domain.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

class ApplyPostRequestDto(
    @field: NotBlank
    val profileId: Long,
    @field: NotNull
    val content: String
) {
}
