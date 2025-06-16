package kr.co.programmers.collabond.api.profiletag.domain

import jakarta.persistence.*
import kr.co.programmers.collabond.api.profile.domain.Profile
import kr.co.programmers.collabond.api.tag.domain.Tag
import lombok.Builder

@Entity
@Table(name = "profile_tags")
class ProfileTag @Builder private constructor(
    @field:JoinColumn(
        name = "profile_id",
        nullable = false
    ) @field:ManyToOne(fetch = FetchType.LAZY) private var profile: Profile, @field:JoinColumn(
        name = "tag_id",
        nullable = false
    ) @field:ManyToOne(fetch = FetchType.LAZY) var tag: Tag
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private val id: Long? = null

    fun updateProfile(profile: Profile) {
        this.profile = profile
    }
}
