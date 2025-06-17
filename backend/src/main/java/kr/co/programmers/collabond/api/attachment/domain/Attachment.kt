package kr.co.programmers.collabond.api.attachment.domain

import jakarta.persistence.*
import kr.co.programmers.collabond.api.apply.domain.ApplyPost
import kr.co.programmers.collabond.api.file.domain.File

@Entity
@Table(name = "attachments")
class Attachment(
    @field:Id
    @field:GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @field:JoinColumn(
        name = "apply_post_id",
        nullable = false
    )
    @field:ManyToOne(fetch = FetchType.LAZY)
    var applyPost: ApplyPost,

    @field:JoinColumn(
        name = "file_id",
        nullable = false
    )
    @field:OneToOne(fetch = FetchType.LAZY)
    var file: File
) {

    fun addApplyPost(applyPost: ApplyPost) {
        this.applyPost = applyPost
    }
}