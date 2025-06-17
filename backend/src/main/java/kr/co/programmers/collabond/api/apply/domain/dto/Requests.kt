package kr.co.programmers.collabond.api.apply.domain.dto

import jakarta.validation.constraints.Size

class ApplyPostRequestDto(
    val profileId: Long,
    @field: Size(max = 1000)
    val content: String
) {
}

class ReceivedApplyPostsRequestDto(
    val status: String? = null,
    val sort: String? = null
) {
}

class SentApplyPostsRequestDto(
    val status: String? = null,
    val sort: String? = null
) {
}