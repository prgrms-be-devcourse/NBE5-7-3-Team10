package kr.co.programmers.collabond.api.attachment.interfaces

import kr.co.programmers.collabond.api.apply.domain.ApplyPost
import kr.co.programmers.collabond.api.attachment.domain.Attachment
import kr.co.programmers.collabond.api.attachment.domain.dto.AttachmentDto
import kr.co.programmers.collabond.api.file.domain.File

object AttachmentMapper {
    fun toEntity(applyPost: ApplyPost, file: File): Attachment {
        return Attachment(
            applyPost = applyPost,
            file = file
        )
    }

    fun toDto(attachment: Attachment): AttachmentDto {
        return AttachmentDto(
            originName = attachment.file.originName,
            savedName = attachment.file.savedName
        )
    }
}
