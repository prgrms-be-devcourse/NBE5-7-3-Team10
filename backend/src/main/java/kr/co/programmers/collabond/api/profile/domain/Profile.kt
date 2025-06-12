package kr.co.programmers.collabond.api.profile.domain

import jakarta.persistence.*
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
@Getter
@SQLDelete(sql = "UPDATE profiles SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class Profile @Builder constructor(
    @JvmField @field:JoinColumn(
        name = "user_id",
        nullable = false
    ) @field:ManyToOne(fetch = FetchType.LAZY) private var user: User,
    private var addressCode: String,
    private var address: String,
    @field:Enumerated(
        EnumType.STRING
    ) @field:Column(nullable = false) private var type: ProfileType,
    @field:Column(
        nullable = false
    ) private var name: String,
    @field:Column(nullable = false, length = 1000) private var description: String,
    collaboCount: Int,
    status: Boolean
) : UpdatedEntity() {
    @Column(name = "collabo_count", nullable = false)
    private var collaboCount = 0

    @OneToMany(mappedBy = "profile", cascade = [CascadeType.ALL], orphanRemoval = true)
    private val images: MutableList<Image> = ArrayList()

    @OneToMany(mappedBy = "profile", cascade = [CascadeType.ALL], orphanRemoval = true)
    private val tags: MutableList<ProfileTag> = ArrayList()

    @OneToMany(mappedBy = "profile")
    private val applyPosts: List<ApplyPost> = ArrayList()

    @OneToMany(mappedBy = "profile")
    private val recruitPosts: List<RecruitPost> = ArrayList()

    @Column(nullable = false)
    private var status: Boolean

    init {
        this.collaboCount = collaboCount
        this.status = status
    }

    fun update(
        name: String?, description: String?, addressCode: String?, address: String,
        collaboCount: Int?, status: Boolean?
    ): Profile {
        if (name != null) {
            this.name = name
        }

        if (description != null) {
            this.description = description
        }

        if (addressCode != null) {
            this.addressCode = address
        }

        if (collaboCount != null) {
            this.collaboCount = collaboCount
        }

        if (status != null) {
            this.status = status
        }

        return this
    }

    val displayName: String
        get() = if (isDeleted) "(이름없음)" else name

    fun isStatus(): Boolean {
        return java.lang.Boolean.TRUE == this.status
    }

    fun update(name: String?, description: String?, addressCode: String?, address: String?, status: Boolean) {
        if (name != null) {
            this.name = name
        }

        if (description != null) {
            this.description = description
        }

        if (addressCode != null) {
            this.addressCode = addressCode
        }

        if (address != null) {
            this.address = address
        }
        this.status = status
    }

    fun addImage(image: Image) {
        images.add(image)
        image.updateProfile(this)
    }

    fun addTag(tag: ProfileTag) {
        tags.add(tag)
        tag.updateProfile(this)
    }

    fun updateCollaboCount() {
        this.collaboCount = this.collaboCount + 1
    }
}