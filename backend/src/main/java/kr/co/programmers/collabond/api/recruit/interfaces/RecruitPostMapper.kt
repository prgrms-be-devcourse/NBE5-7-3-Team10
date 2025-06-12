package kr.co.programmers.collabond.api.recruit.interfaces

import kr.co.programmers.collabond.api.profile.domain.Profile
import kr.co.programmers.collabond.api.profile.interfaces.ProfileMapper
import kr.co.programmers.collabond.api.recruit.domain.RecruitPost
import kr.co.programmers.collabond.api.recruit.domain.RecruitPostStatus
import kr.co.programmers.collabond.api.recruit.domain.dto.RecruitPostDto
import kr.co.programmers.collabond.api.recruit.domain.dto.RecruitPostRequestDto
import kr.co.programmers.collabond.api.recruit.domain.dto.RecruitPostResponseDto

object RecruitPostMapper {

    fun toDto(entity: RecruitPost): RecruitPostDto = RecruitPostDto(
        id = entity.id!!,
        title = entity.title,
        description = entity.description,
        status = entity.status.toString(),
        deadline = entity.deadline,
        writerProfileId = entity.profile?.id!!,
        writerProfileName = entity.profile?.name!!
    )

    fun toResponseDto(entity: RecruitPost, fullPath: String): RecruitPostResponseDto =
        RecruitPostResponseDto(
            id = entity.id!!,
            title = entity.title,
            description = entity.description,
            status = entity.status,
            deadline = entity.deadline,
            profileId = entity.profile?.id!!,
            profileName = entity.profile?.name!!,
            profile = ProfileMapper.toSimpleDto(entity.profile!!, fullPath),
            createdAt = entity.createdAt!!,
            deletedAt = entity.deletedAt
        )

    fun toEntity(dto: RecruitPostRequestDto, profile: Profile): RecruitPost = RecruitPost(
        profile = profile,
        title = dto.title,
        description = dto.description,
        deadline = dto.deadline,
        status = dto.status?.takeIf { it.isNotEmpty() }
            ?.let(RecruitPostStatus::valueOf)
            ?: RecruitPostStatus.RECRUITING
    )
}