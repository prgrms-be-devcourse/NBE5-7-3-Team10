package kr.co.programmers.collabond.api.profile.infrastructure;

import kr.co.programmers.collabond.api.profile.domain.Profile;
import kr.co.programmers.collabond.api.profile.domain.ProfileType;
import kr.co.programmers.collabond.api.user.domain.Role;
import kr.co.programmers.collabond.api.user.domain.User;
import kr.co.programmers.collabond.config.TestAuditingConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@DataJpaTest
//@EntityScan(basePackageClasses = {Profile.class, User.class})
//@EntityScan("kr.co.programmers.collabond")
@Import(TestAuditingConfig.class)
class ProfileRepositoryTest {

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private TestEntityManager em;


    private User createUser(String email, String nickname, Role role, String providerId) {
        User user = User.builder()
                .email(email)
                .nickname(nickname)
                .role(role)
                .providerId(providerId)
                .build();
         em.persist(user);
         return user;
    }


    @Test
    @DisplayName("userId로 모든 Profile 조회")
    void findAllByUserId() {

        //given
        User user1 = createUser("user1@test.com", "yewon", Role.TMP, "1234");

        Profile p1 = Profile.builder()
                .user(user1)
                .name("p1")
                .type(ProfileType.IP)
                .description("desc")
                .collaboCount(0)
                .status(true)
                .build();

        Profile p2 = Profile.builder()
                .user(user1)
                .name("p2")
                .type(ProfileType.IP)
                .description("desc")
                .collaboCount(0)
                .status(true)
                .build();



        em.persist(p1);
        em.persist(p2);

        em.flush();
        em.clear();

        // when
        List<Profile> results = profileRepository.findAllByUserId(user1.getId());

        // then
        assertThat(results).hasSize(2);
        assertThat(results).extracting(Profile::getName).containsExactlyInAnyOrder("p1", "p2");

    }

    @Test
    @DisplayName("userId로 Profile 개수 조회")
    void countByUserId() {
        //given
        User user1 = createUser("user1@test.com", "yewon", Role.TMP, "1234");

        Profile p1 = Profile.builder()
                .user(user1)
                .name("p1")
                .type(ProfileType.IP)
                .description("desc")
                .collaboCount(0)
                .status(true)
                .build();

        Profile p2 = Profile.builder()
                .user(user1)
                .name("p2")
                .type(ProfileType.IP)
                .description("desc")
                .collaboCount(0)
                .status(true)
                .build();



        em.persist(p1);
        em.persist(p2);

        em.flush();
        em.clear();


        //when
        long count = profileRepository.countByUserId(user1.getId());

        // then
        assertThat(count).isEqualTo(2);

    }
}