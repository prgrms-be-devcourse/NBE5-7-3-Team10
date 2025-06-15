package kr.co.programmers.collabond.api.user;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import kr.co.programmers.collabond.api.user.domain.Role;
import kr.co.programmers.collabond.api.user.domain.User;
import kr.co.programmers.collabond.api.user.infrastructure.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class SoftDeleteTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @Transactional
    @DisplayName("User Soft Delete 테스트")
    void testSoftDelete() {
        // given
        final String TEST_EMAIL = "test@example.com";

        User user = User.builder()
                .email(TEST_EMAIL)
                .nickname("테스트유저")
                .role(Role.ROLE_IP)
                .build();
        userRepository.save(user);
        entityManager.flush();
        entityManager.clear();

        // when
        userRepository.deleteByEmail(TEST_EMAIL);
        entityManager.flush();
        entityManager.clear();

        // then
        // 1. 일반 조회 시 조회되지 않아야 함
        assertThat(userRepository.findByEmail(TEST_EMAIL).isEmpty()).isTrue();

        // 2. 네이티브 쿼리로 조회 시 레코드는 존재하고 deleted_at이 설정되어 있어야 함
        Optional<User> found = userRepository.findByEmailAndDeletedAtIsNotNull(TEST_EMAIL);
        assertThat(found.isPresent()).isTrue();
        assertThat(found.get().getDeletedAt()).isNotNull();
    }
}
