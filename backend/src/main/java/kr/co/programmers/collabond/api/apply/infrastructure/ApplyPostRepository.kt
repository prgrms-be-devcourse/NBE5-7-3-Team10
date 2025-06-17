package kr.co.programmers.collabond.api.apply.infrastructure

import kr.co.programmers.collabond.api.apply.domain.ApplyPost
import kr.co.programmers.collabond.api.apply.domain.dto.ApplyPostDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface ApplyPostRepository : JpaRepository<ApplyPost, Long> {

    fun findByProfileId(
        profileId: Long
    ): List<ApplyPost>

    // 보낸 applyPost를 보는 것이기 때문에 status는 applyPost의 상태
    @Query(
        """
            SELECT new kr.co.programmers.collabond.api.apply.domain.dto.ApplyPostDto(a)
            FROM ApplyPost a
            WHERE a.status = :status and a.profile.user.id = :userId
        """
    )
    fun findAllSentByUserIdAndStatus(
        userId: Long,
        status: String,
        pageable: Pageable
    ): Page<ApplyPostDto>

    @Query(
        """
            SELECT new kr.co.programmers.collabond.api.apply.domain.dto.ApplyPostDto(a)
            FROM ApplyPost a
            WHERE a.profile.user.id = :userId
        """
    )
    fun findAllSentByUser(
        userId: Long,
        pageable: Pageable
    ): Page<ApplyPostDto>


    // 받은 applyPost를 보는 것이기 때문에 status는 recruitPost의 상태
    @Query(
        """
            SELECT new kr.co.programmers.collabond.api.apply.domain.dto.ApplyPostDto(a)
            FROM ApplyPost a
            WHERE a.recruitPost.status = :status and a.recruitPost.profile.user.id = :userId
        """
    )
    fun findAllReceivedByUserIdAndStatus(
        userId: Long,
        status: String,
        pageable: Pageable
    ): Page<ApplyPostDto>

    @Query(
        """
            SELECT new kr.co.programmers.collabond.api.apply.domain.dto.ApplyPostDto(a)
            FROM ApplyPost a
            WHERE a.recruitPost.profile.user.id = :userId
        """
    )
    fun findAllReceivedByUser(
        userId: Long,
        pageable: Pageable
    ): Page<ApplyPostDto>
}
