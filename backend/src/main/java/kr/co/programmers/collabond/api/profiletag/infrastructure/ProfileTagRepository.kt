package kr.co.programmers.collabond.api.profiletag.infrastructure

import kr.co.programmers.collabond.api.profile.domain.Profile
import kr.co.programmers.collabond.api.profiletag.domain.ProfileTag
import org.springframework.data.jpa.repository.JpaRepository

interface ProfileTagRepository : JpaRepository<ProfileTag?, Long?> {
    // 특정 프로필의 모든 태그 삭제
    fun deleteAllByProfile(profile: Profile?)
}
