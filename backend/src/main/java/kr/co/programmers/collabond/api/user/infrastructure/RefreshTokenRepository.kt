package kr.co.programmers.collabond.api.user.infrastructure

import kr.co.programmers.collabond.api.user.domain.RefreshToken
import kr.co.programmers.collabond.api.user.domain.TokenStatus
import kr.co.programmers.collabond.api.user.domain.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface RefreshTokenRepository : JpaRepository<RefreshToken, Long> {
    fun findByToken(token: String): RefreshToken?
    fun findByUserAndStatus(user: User, status: TokenStatus): RefreshToken?
}