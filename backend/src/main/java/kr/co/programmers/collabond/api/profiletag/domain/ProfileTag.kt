package kr.co.programmers.collabond.api.profiletag.domain

import jakarta.persistence.*
import kr.co.programmers.collabond.api.profile.domain.Profile
import kr.co.programmers.collabond.api.tag.domain.Tag


@Entity
@Table(name = "profile_tags")
class ProfileTag(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    var profile: Profile? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", nullable = false)
    val tag: Tag,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null
) {

    constructor(tag: Tag) : this(null, tag)

}

