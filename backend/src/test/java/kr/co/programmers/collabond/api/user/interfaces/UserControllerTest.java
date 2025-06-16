package kr.co.programmers.collabond.api.user.interfaces;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.programmers.collabond.api.user.application.UserService;
import kr.co.programmers.collabond.api.user.domain.Role;
import kr.co.programmers.collabond.api.user.domain.dto.SignUpResponseDto;
import kr.co.programmers.collabond.api.user.domain.dto.UserSignUpRequestDto;
import kr.co.programmers.collabond.core.auth.jwt.TokenService;
import kr.co.programmers.collabond.util.WithCustomMockUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private TokenService tokenService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @WithCustomMockUser(username = "testProviderId", role = Role.ROLE_STORE)
    @DisplayName("회원가입 API 성공 테스트")
    void signupSuccess() throws Exception {
        // given
        String providerId = "testProviderId";
        String nickname = "testNickname";
        String accessToken = "testAccessToken";
        Role role = Role.ROLE_IP;

        UserSignUpRequestDto requestDto = UserSignUpRequestDto.builder()
                .nickname(nickname)
                .role(role)
                .build();

        SignUpResponseDto responseDto = SignUpResponseDto.builder()
                .nickname(nickname)
                .role(role.name())
                .accessToken(accessToken)
                .build();

        when(userService.signup(providerId, requestDto)).thenReturn(responseDto);

        // when, then
        mockMvc.perform(patch("/api/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname").value(nickname))
                .andExpect(jsonPath("$.role").value(role.name()))
                .andExpect(jsonPath("$.accessToken").value(accessToken))
                .andDo(print());
    }

    @Test
    @DisplayName("회원가입 시 Role 미입력 실패 테스트")
    void signupInvalidRoleInput() throws Exception {
        // given
        UserSignUpRequestDto requestDto = UserSignUpRequestDto.builder()
                .nickname("testProviderId")
                .role(null)
                .build();

        // when, then
        mockMvc.perform(patch("/api/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "  ", "abcdefghijklmnopqrstuvwxyz", "ab bc", "abc!23$%"})
    @DisplayName("회원가입 시 invalid 닉네임 실패 테스트")
    void signupInvalidNicknameInput(String nickname) throws Exception {
        // given
        UserSignUpRequestDto requestDto = UserSignUpRequestDto.builder()
                .nickname(nickname)
                .role(Role.ROLE_IP)
                .build();

        // when, then
        mockMvc.perform(patch("/api/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }
}