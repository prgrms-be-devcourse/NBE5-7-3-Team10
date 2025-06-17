package kr.co.programmers.collabond.api.apply.interfaces

import kr.co.programmers.collabond.api.apply.domain.ApplyPost
import kr.co.programmers.collabond.api.apply.domain.ApplyPostStatus
import kr.co.programmers.collabond.api.apply.domain.dto.ApplyPostDto
import kr.co.programmers.collabond.api.apply.domain.dto.ApplyPostRequestDto
import kr.co.programmers.collabond.api.attachment.interfaces.AttachmentMapper
import kr.co.programmers.collabond.api.profile.domain.Profile
import kr.co.programmers.collabond.api.profile.interfaces.ProfileMapper
import kr.co.programmers.collabond.api.recruit.domain.RecruitPost
import kr.co.programmers.collabond.api.recruit.interfaces.RecruitPostMapper

object ApplyPostMapper {

    fun toEntity(
        recruitPost: RecruitPost,
        profile: Profile,
        request: ApplyPostRequestDto
    ): ApplyPost = ApplyPost(
        recruitPost = recruitPost,
        profile = profile,
        content = request.content,
        status = ApplyPostStatus.PENDING,
    )

    fun toDto(entity: ApplyPost): ApplyPostDto = ApplyPostDto(
        id = entity.id,
        recruitPost = RecruitPostMapper.toDto(entity.recruitPost),
        profile = ProfileMapper.toDto(entity.profile),
        content = entity.content,
        status = entity.status.name,
        attachmentFiles = entity.attachments?.map { AttachmentMapper.toDto(it) }?.toMutableList(),
        createdAt = entity.createdAt
    )

}
