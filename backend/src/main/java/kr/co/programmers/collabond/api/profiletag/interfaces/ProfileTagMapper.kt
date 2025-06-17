package kr.co.programmers.collabond.api.profiletag.interfaces

import kr.co.programmers.collabond.api.profiletag.domain.ProfileTag
import kr.co.programmers.collabond.api.tag.domain.Tag


object ProfileTagMapper {

    @JvmStatic
    fun toEntity(tag: Tag): ProfileTag =
        ProfileTag(tag)
}

