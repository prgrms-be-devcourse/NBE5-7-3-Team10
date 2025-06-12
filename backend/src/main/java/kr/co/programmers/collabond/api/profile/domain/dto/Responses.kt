package kr.co.programmers.collabond.api.profile.domain.dto

import kr.co.programmers.collabond.api.tag.domain.dto.TagResponseDto
import java.time.LocalDateTime

data class ProfileDetailResponseDto(
    val id: Long,
    val userId: Long,
    val nickname: String,
    val type: String,
    val name: String,
    val profileImageUrl: String?,
    val thumbnailImageUrl: String?,
    val extraImageUrls: List<String>,
    val description: String,
    val address: String,
    val addressCode: String,
    val collaboCount: Int,
    val status: Boolean,
    val tags: List<TagResponseDto>,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)


data class ProfileDto(
    val id: Long,
    val name: String,
    val description: String,
    val type: String,
    val imageUrl: String,
    val collaboCount: Int,
    val userId: Long
)

data class ProfileResponseDto(
    val id: Long,
    val userId: Long,
    val type: String,
    val name: String,
    val description: String,
    val address: String,
    val addressCode: String,
    val collaboCount: Int,
    val status: Boolean,
    val tags: List<TagResponseDto>,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val profileImageUrl: String?,
    val thumbnailImageUrl: String?,
    val extraImageUrls: List<String>
)


data class ProfileSimpleResponseDto(
    val profileId: Long,
    val type: String,
    val imageUrl: String,
    val address: String,
    val status: Boolean
)
