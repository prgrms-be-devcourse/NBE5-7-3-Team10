package kr.co.programmers.collabond.api.profile.domain

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import kr.co.programmers.collabond.api.apply.domain.ApplyPost
import kr.co.programmers.collabond.api.image.domain.Image
import kr.co.programmers.collabond.api.profiletag.domain.ProfileTag
import kr.co.programmers.collabond.api.recruit.domain.RecruitPost
import kr.co.programmers.collabond.api.user.domain.User
import kr.co.programmers.collabond.shared.domain.UpdatedEntity
import org.hibernate.annotations.SQLDelete
import org.hibernate.annotations.SQLRestriction

@Entity
@Table(name = "profiles")
@SQLDelete(sql = "UPDATE profiles SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
class Profile(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Column
    var addressCode: String,

    @Column
    var address: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val type: ProfileType,

    @Column(nullable = false)
    var name: String,

    @Column(nullable = false, length = 1000)
    var description: String,

    @Column(name = "collabo_count", nullable = false)
    var collaboCount: Int = 0,
    @Column(nullable = false)
    var status: Boolean = true
) : UpdatedEntity() {

    @OneToMany(mappedBy = "profile", cascade = [CascadeType.ALL], orphanRemoval = true)
    val images: MutableList<Image> = mutableListOf()

    @OneToMany(mappedBy = "profile", cascade = [CascadeType.ALL], orphanRemoval = true)
    val tags: MutableList<ProfileTag> = mutableListOf()

    @OneToMany(mappedBy = "profile")
    val applyPosts: MutableList<ApplyPost> = mutableListOf()

    @OneToMany(mappedBy = "profile")
    val recruitPosts: MutableList<RecruitPost> = mutableListOf()

    fun update(
        name: String?,
        description: String?,
        addressCode: String?,
        address: String?,
        status: Boolean
    ) {
        name?.let { this.name = it }
        description?.let { this.description = it }
        addressCode?.let { this.addressCode = it }
        address?.let { this.address = it }
        this.status = status
    }

    fun addImage(image: Image) {
        images.add(image)
        image.updateProfile(this)
    }

    fun addTag(tag: ProfileTag) {
        tags.add(tag)
        tag.setProfile(this)
    }

    fun updateCollaboCount() {
        collaboCount += 1
    }
}