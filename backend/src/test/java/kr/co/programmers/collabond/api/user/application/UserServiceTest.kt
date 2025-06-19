package kr.co.programmers.collabond.api.user.application

import kr.co.programmers.collabond.api.user.domain.Role
import kr.co.programmers.collabond.api.user.domain.User
import kr.co.programmers.collabond.api.user.domain.UserSignUpRequestDto
import kr.co.programmers.collabond.api.user.domain.SignUpResponseDto
import kr.co.programmers.collabond.api.user.infrastructure.UserRepository
import kr.co.programmers.collabond.core.auth.jwt.TokenService
import kr.co.programmers.collabond.shared.exception.InvalidException
import kr.co.programmers.collabond.shared.exception.NotFoundException
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever

@ExtendWith(MockitoExtension::class)
class UserServiceTest {

    @InjectMocks
    private lateinit var userService: UserService

    @Mock
    private lateinit var userRepository: UserRepository

    @Mock
    private lateinit var tokenService: TokenService

    @Test
    @DisplayName("회원가입 성공 테스트")
    fun signupSuccess() {
        // given
        val providerId = "testProviderId"
        val nickname = "testNickname"
        val role = Role.ROLE_IP
        val requestDto = UserSignUpRequestDto(nickname, role)
        val foundUser = User(
            email = "",
            nickname = "임시닉네임",
            role = Role.ROLE_TMP,
            providerId = providerId,
            profiles = mutableListOf()
        )
        val reissuedAccessToken = "testAccessToken"

        whenever(userRepository.findByProviderId(providerId))
            .thenReturn(foundUser)
        whenever(tokenService.createAccessToken(providerId, requestDto.role))
            .thenReturn(reissuedAccessToken)

        // when
        val responseDto: SignUpResponseDto = userService.signup(providerId, requestDto)

        // then
        assertAll(
            { kotlin.test.assertEquals(nickname, responseDto.nickname) },
            { kotlin.test.assertEquals(role.name, responseDto.role) },
            { kotlin.test.assertEquals(reissuedAccessToken, responseDto.accessToken) }
        )
    }

    @Test
    @DisplayName("존재하지 않는 사용자 회원가입 시도 시 예외 발생")
    fun signupUserNotFound() {
        // given
        val providerId = "nonExistentProviderId"
        val requestDto = UserSignUpRequestDto("testNickname", Role.ROLE_IP)

        whenever(userRepository.findByProviderId(providerId))
            .thenReturn(null)

        // when, then
        assertThrows<NotFoundException> {
            userService.signup(providerId, requestDto)
        }
    }

    @Test
    @DisplayName("이미 가입된 사용자의 회원가입 시도 시 예외 발생")
    fun signupAlreadyRegistered() {
        // given
        val providerId = "testProviderId"
        val requestDto = UserSignUpRequestDto("nickname", Role.ROLE_IP)
        val existingUser = User(
            email = "",
            nickname = "nickname",
            role = Role.ROLE_IP,
            providerId = providerId
        )

        whenever(userRepository.findByProviderId(providerId))
            .thenReturn(existingUser)

        // when, then
        assertThrows<InvalidException> {
            userService.signup(providerId, requestDto)
        }
    }
}