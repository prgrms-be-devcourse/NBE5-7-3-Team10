package kr.co.programmers.collabond.api.profile.infrastructure

import kr.co.programmers.collabond.api.profile.domain.Profile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor

interface ProfileRepository : JpaRepository<Profile, Long>,
    JpaSpecificationExecutor<Profile> {
    fun findAllByUserId(userId: Long): List<Profile>
    fun countByUserId(userId: Long): Long
}