package kr.co.programmers.collabond.api.recruit.domain

import jakarta.persistence.*
import kr.co.programmers.collabond.api.profile.domain.Profile
import kr.co.programmers.collabond.shared.domain.UpdatedEntity
import org.hibernate.annotations.SQLDelete
import org.hibernate.annotations.SQLRestriction
import java.time.LocalDateTime

@Entity
@Table(name = "recruit_posts")
@SQLDelete(sql = "UPDATE recruit_posts SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
class RecruitPost(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id")
    var profile: Profile? = null,

    @Column(nullable = false)
    var title: String,

    @Column(nullable = false, length = 1000)
    var description: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: RecruitPostStatus = RecruitPostStatus.RECRUITING,

    @Column(nullable = false)
    var deadline: LocalDateTime
) : UpdatedEntity() {

    protected constructor() : this(
        profile = null,
        title = "",
        description = "",
        deadline = LocalDateTime.now()
    )

    fun update(
        title: String? = null,
        description: String? = null,
        status: RecruitPostStatus? = null,
        deadline: LocalDateTime? = null
    ): RecruitPost = apply {
        title?.let { this.title = it }
        description?.let { this.description = it }
        status?.let { this.status = it }
        deadline?.let { this.deadline = it }
    }
}