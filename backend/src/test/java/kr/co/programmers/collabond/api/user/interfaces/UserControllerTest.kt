package kr.co.programmers.collabond.api.user.interfaces

import com.fasterxml.jackson.databind.ObjectMapper
import kr.co.programmers.collabond.api.user.application.UserService
import kr.co.programmers.collabond.api.user.domain.Role
import kr.co.programmers.collabond.api.user.domain.SignUpResponseDto
import kr.co.programmers.collabond.api.user.domain.UserSignUpRequestDto
import kr.co.programmers.collabond.core.auth.jwt.TokenService
import kr.co.programmers.collabond.util.WithCustomMockUser
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(UserController::class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper
) {

    @MockitoBean
    private lateinit var userService: UserService

    @MockitoBean
    private lateinit var tokenService: TokenService

    @Test
    @WithCustomMockUser(username = "testProviderId", role = Role.ROLE_STORE)
    @DisplayName("회원가입 API 성공 테스트")
    fun signupSuccess() {
        // given
        val providerId = "testProviderId"
        val nickname = "testNickname"
        val accessToken = "testAccessToken"
        val role = Role.ROLE_IP
        val requestDto = UserSignUpRequestDto(nickname, role)
        val responseDto = SignUpResponseDto(nickname, role.name, accessToken)

        whenever(userService.signup(providerId, requestDto))
            .thenReturn(responseDto)

        // when, then
        mockMvc.perform(
            patch("/api/users/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.nickname").value(nickname))
            .andExpect(jsonPath("$.role").value(role.name))
            .andExpect(jsonPath("$.accessToken").value(accessToken))
            .andDo(print())
    }

    @Test
    @DisplayName("회원가입 시 Role 미입력 실패 테스트")
    fun signupInvalidRoleInput() {
        mockMvc.perform(
            patch("/api/users/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "nickname": "testNickname"
                    }
                """.trimIndent())
        )
            .andExpect(status().isBadRequest)
            .andDo(print())
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "  ", "abcdefghijklmnopqrstuvwxyz", "ab bc", "abc!23$%"])
    @DisplayName("회원가입 시 invalid 닉네임 실패 테스트")
    fun signupInvalidNicknameInput(nickname: String) {
        val requestDto = UserSignUpRequestDto(nickname, Role.ROLE_IP)

        mockMvc.perform(
            patch("/api/users/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto))
        )
            .andExpect(status().isBadRequest)
            .andDo(print())
    }
}