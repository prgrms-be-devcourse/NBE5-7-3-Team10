package kr.co.programmers.collabond.api.apply.domain.dto

import kr.co.programmers.collabond.api.apply.domain.ApplyPost
import kr.co.programmers.collabond.api.apply.interfaces.ApplyPostMapper
import kr.co.programmers.collabond.api.attachment.domain.dto.AttachmentDto
import kr.co.programmers.collabond.api.profile.domain.dto.ProfileDto
import kr.co.programmers.collabond.api.recruit.domain.dto.RecruitPostDto
import java.time.LocalDateTime

data class ApplyPostDto(
    var id: Long,
    var recruitPost: RecruitPostDto?,
    var profile: ProfileDto?,
    var content: String,
    var status: String,
    var attachmentFiles: MutableList<AttachmentDto>?,
    var createdAt: LocalDateTime,
) {

    constructor(
        applyPost: ApplyPost
    ) : this(
        applyPost.id,
        null,
        null,
        applyPost.content,
        applyPost.status.toString(),
        null,
        applyPost.createdAt
    ) {
        val a = ApplyPostMapper.toDto(applyPost)
        this.recruitPost = a.recruitPost
        this.profile = a.profile
        this.attachmentFiles = a.attachmentFiles
    }
}
