package kr.co.programmers.collabond.api.apply.domain

import jakarta.persistence.*
import jakarta.validation.constraints.NotNull
import kr.co.programmers.collabond.api.attachment.domain.Attachment
import kr.co.programmers.collabond.api.profile.domain.Profile
import kr.co.programmers.collabond.api.recruit.domain.RecruitPost
import kr.co.programmers.collabond.shared.domain.OnlyCreatedEntity

@Entity
@Table(name = "apply_posts")
class ApplyPost(
    @field:NotNull
    @field:JoinColumn(
        name = "recruit_post_id",
        nullable = false
    )
    @field:ManyToOne(fetch = FetchType.LAZY)
    var recruitPost: RecruitPost,

    @field:NotNull
    @field:JoinColumn(name = "profile_id")
    @field:ManyToOne(fetch = FetchType.LAZY)
    var profile: Profile,

    @field:Column(
        nullable = false,
        length = 1000
    )
    var content: String,

    @field:Column(nullable = false)
    @field:Enumerated(EnumType.STRING)
    var status: ApplyPostStatus,

    @OneToMany(
        mappedBy = "applyPost",
        cascade = [CascadeType.ALL]
    )
    var attachments: MutableList<Attachment>? = null

) : OnlyCreatedEntity() {

    // 연관관계 편의 메서드
    fun updateAttachment(attachments: MutableList<Attachment>) {
        this.attachments = attachments
        for (attachment in attachments) {
            attachment.addApplyPost(this)
        }
    }

    fun updateStatus(status: ApplyPostStatus) {
        this.status = status
    }
}
