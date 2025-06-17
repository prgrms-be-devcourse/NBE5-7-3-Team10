package kr.co.programmers.collabond.api.recruit.domain.dto

import jakarta.validation.constraints.*
import java.time.LocalDateTime

data class Requests(
    @field:Positive(message = "프로필 ID는 양수여야 합니다")
    val profileId: Long,

    @field:Size(max = 100, message = "제목은 100자 이하여야 합니다")
    val title: String,

    @field:Size(max = 1000, message = "설명은 1000자 이하여야 합니다")
    val description: String,

    @field:Future(message = "마감일은 현재 시간 이후여야 합니다")
    val deadline: LocalDateTime,

    @field:Pattern(
        regexp = "^(RECRUITING|COMPLETED|CLOSED)$",
        message = "상태는 RECRUITING, COMPLETED, CLOSED 중 하나여야 합니다"
    )
    val status: String? = null
)