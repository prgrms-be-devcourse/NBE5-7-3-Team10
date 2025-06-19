package kr.co.programmers.collabond.api.user

import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional
import kr.co.programmers.collabond.api.user.domain.Role
import kr.co.programmers.collabond.api.user.domain.User
import kr.co.programmers.collabond.api.user.infrastructure.UserRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class SoftDeleteTest @Autowired constructor(
    private val userRepository: UserRepository,
    private val entityManager: EntityManager
) {

    @Test
    @Transactional
    @DisplayName("User Soft Delete 테스트")
    fun testSoftDelete() {
        // given
        val testEmail = "test@example.com"
        val user = User(
            email = testEmail,
            nickname = "테스트유저",
            role = Role.ROLE_IP,
        )
        userRepository.save(user)
        entityManager.flush()
        entityManager.clear()

        // when
        userRepository.deleteByEmail(testEmail)
        entityManager.flush()
        entityManager.clear()

        // then
        // 1. 일반 조회 시 조회되지 않아야 함
        assertThat(userRepository.findByEmail(testEmail)).isNull()

        // 2. 네이티브 쿼리로 조회 시 레코드는 존재하고 deletedAt이 설정되어 있어야 함
        val found = userRepository.findByEmailAndDeletedAtIsNotNull(testEmail)
        assertThat(found).isNotNull
        assertThat(found!!.deletedAt).isNotNull
    }
}