package kr.co.programmers.collabond.api.user.infrastructure

import kr.co.programmers.collabond.api.user.domain.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface UserRepository : JpaRepository<User, Long> {
    @Query(
        value = """
            SELECT * 
            FROM users 
            WHERE email = :email AND deleted_at is not null
            """,
        nativeQuery = true
    )
    fun findByEmailAndDeletedAtIsNotNull(email: String): User?

    fun findByEmail(email: String): User?

    fun findByProviderId(providerId: String): User?

    fun deleteByEmail(email: String)
}