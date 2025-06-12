package kr.co.programmers.collabond.api.user.infrastructure;

import kr.co.programmers.collabond.api.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    @Query(value = """
            SELECT * 
            FROM users 
            WHERE email = :email AND deleted_at is not null
            """,
            nativeQuery = true)
    Optional<User> findByEmailAndDeletedAtIsNotNull(String email);

    Optional<User> findByEmail(String email);

    Optional<User> findByProviderId(String providerId);

    void deleteByEmail(String email);

}
