package kr.co.programmers.collabond.api.profiletag.domain

import jakarta.persistence.*
import kr.co.programmers.collabond.api.profile.domain.Profile
import kr.co.programmers.collabond.api.tag.domain.Tag
import lombok.Builder

@Entity
@Table(name = "profile_tags")
class ProfileTag() {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    private var profile: Profile? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", nullable = false)
    var tag: Tag? = null

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private val id: Long? = null


    protected constructor(dummy: Boolean) : this()

    constructor(tag: Tag) : this() {
        this.tag = tag
    }
    fun updateProfile(profile: Profile) {
        this.profile = profile
    }
}
