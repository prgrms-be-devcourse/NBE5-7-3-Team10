package kr.co.programmers.collabond.api.recruit.domain.dto

import kr.co.programmers.collabond.api.profile.domain.dto.ProfileSimpleResponseDto
import kr.co.programmers.collabond.api.recruit.domain.RecruitPostStatus
import java.time.LocalDateTime

data class Responses(
    val id: Long,
    val title: String,
    val description: String,
    val status: RecruitPostStatus,
    val deadline: LocalDateTime,
    val profileId: Long,
    val profileName: String,
    val profile: ProfileSimpleResponseDto,
    val createdAt: LocalDateTime,
    val deletedAt: LocalDateTime? = null
)

data class RecruitPostDto(
    val id: Long,
    val title: String,
    val description: String,
    val status: String,
    val deadline: LocalDateTime,
    val writerProfileId: Long,
    val writerProfileName: String
)