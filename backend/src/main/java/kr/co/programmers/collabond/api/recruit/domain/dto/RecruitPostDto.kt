package kr.co.programmers.collabond.api.recruit.domain.dto

import java.time.LocalDateTime

data class RecruitPostDto(
    val id: Long,
    val title: String,
    val description: String,
    val status: String,
    val deadline: LocalDateTime,
    val writerProfileId: Long,
    val writerProfileName: String
)