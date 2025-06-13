package kr.co.programmers.collabond.api.apply.interfaces

import kr.co.programmers.collabond.api.apply.domain.ApplyPost
import kr.co.programmers.collabond.api.apply.domain.ApplyPostStatus
import kr.co.programmers.collabond.api.apply.domain.dto.ApplyPostDto
import kr.co.programmers.collabond.api.apply.domain.dto.ApplyPostRequestDto
import kr.co.programmers.collabond.api.attachment.domain.Attachment
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
    ): ApplyPost {
        return ApplyPost(
            recruitPost = recruitPost,
            profile = profile,
            content = request.content,
            status = ApplyPostStatus.PENDING,
        )
    }

    fun toDto(entity: ApplyPost): ApplyPostDto {
        return ApplyPostDto(
            id = entity.id,
            recruitPost = RecruitPostMapper.toDto(entity.recruitPost),
            profile = ProfileMapper.toDto(entity.profile),
            content = entity.content,
            status = entity.status.toString(),
            attachmentFiles = (
                    if (entity.attachments == null)
                        null
                    else
                        entity.attachments!!
                            .map { attachment: Attachment ->
                                AttachmentMapper.toDto(
                                    attachment
                                )
                            }
                            .toMutableList()
                    ),
            createdAt = entity.createdAt
        )
    }
}
