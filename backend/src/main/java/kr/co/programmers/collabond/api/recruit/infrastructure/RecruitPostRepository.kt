package kr.co.programmers.collabond.api.recruit.infrastructure

import kr.co.programmers.collabond.api.recruit.domain.RecruitPost
import kr.co.programmers.collabond.api.recruit.domain.RecruitPostStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface RecruitPostRepository : JpaRepository<RecruitPost, Long> {

    @Query("""
        SELECT r 
        FROM RecruitPost r 
        WHERE r.profile.user.id = :userId
    """)
    fun findByUserId(@Param("userId") userId: Long, pageable: Pageable): Page<RecruitPost>

    fun findByProfileId(profileId: Long, pageable: Pageable): Page<RecruitPost>

    fun findByStatus(status: RecruitPostStatus, pageable: Pageable): Page<RecruitPost>

    fun findByDeletedAtIsNull(pageable: Pageable): Page<RecruitPost>
}
