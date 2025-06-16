package kr.co.programmers.collabond.api.user.application;

import kr.co.programmers.collabond.api.user.domain.Role;
import kr.co.programmers.collabond.api.user.domain.User;
import kr.co.programmers.collabond.api.user.domain.dto.SignUpResponseDto;
import kr.co.programmers.collabond.api.user.domain.dto.UserSignUpRequestDto;
import kr.co.programmers.collabond.api.user.infrastructure.UserRepository;
import kr.co.programmers.collabond.core.auth.jwt.TokenService;
import kr.co.programmers.collabond.shared.exception.custom.InvalidException;
import kr.co.programmers.collabond.shared.exception.custom.NotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TokenService tokenService;

    @Test
    @DisplayName("회원가입 성공 테스트")
    void signupSuccess() {
        // given
        String providerId = "testProviderId";
        String nickname = "testNickname";
        Role role = Role.ROLE_IP;

        UserSignUpRequestDto requestDto = UserSignUpRequestDto.builder()
                .nickname(nickname)
                .role(role)
                .build();

        User foundUser = User.builder()
                .nickname("임시닉네임")
                .role(Role.ROLE_TMP)
                .providerId(providerId)
                .build();

        String reissuedAccessToken = "testAccessToken";

        when(userRepository.findByProviderId(providerId))
                .thenReturn(Optional.of(foundUser));
        when(tokenService.createAccessToken(providerId, requestDto.role()))
                .thenReturn(reissuedAccessToken);

        // when
        SignUpResponseDto responseDto = userService.signup(providerId, requestDto);

        // then
        assertAll(
                () -> assertEquals(nickname, responseDto.nickname()),
                () -> assertEquals(role.name(), responseDto.role()),
                () -> assertEquals(reissuedAccessToken, responseDto.accessToken())
        );
    }

    @Test
    @DisplayName("존재하지 않는 사용자 회원가입 시도 시 예외 발생")
    void signupUserNotFound() {
        // given
        String providerId = "nonExistentProviderId";
        UserSignUpRequestDto requestDto = UserSignUpRequestDto.builder()
                .nickname("nickname")
                .role(Role.ROLE_IP)
                .build();

        when(userRepository.findByProviderId(providerId)).thenReturn(Optional.empty());

        // when, then
        assertThrows(NotFoundException.class, () ->
                userService.signup(providerId, requestDto)
        );
    }

    @Test
    @DisplayName("이미 가입된 사용자의 회원가입 시도 시 예외 발생")
    void signupAlreadyRegistered() {
        // given
        String providerId = "testProviderId";
        UserSignUpRequestDto requestDto = UserSignUpRequestDto.builder()
                .nickname("nickname")
                .role(Role.ROLE_IP)
                .build();

        User existingUser = User.builder()
                .email("test@test.com")
                .nickname("nickname")
                .role(Role.ROLE_IP)
                .providerId(providerId)
                .build();

        when(userRepository.findByProviderId(providerId)).thenReturn(Optional.of(existingUser));

        // when, then
        assertThrows(InvalidException.class, () ->
                userService.signup(providerId, requestDto)
        );
    }
}
