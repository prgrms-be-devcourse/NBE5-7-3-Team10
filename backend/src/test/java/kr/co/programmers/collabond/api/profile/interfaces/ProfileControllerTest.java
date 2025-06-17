package kr.co.programmers.collabond.api.profile.interfaces;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.programmers.collabond.api.profile.application.ProfileService;
import kr.co.programmers.collabond.api.profile.domain.ProfileType;
import kr.co.programmers.collabond.api.profile.domain.dto.ProfileDetailResponseDto;
import kr.co.programmers.collabond.api.profile.domain.dto.ProfileRequestDto;
import kr.co.programmers.collabond.api.profile.domain.dto.ProfileResponseDto;
import kr.co.programmers.collabond.api.user.domain.Role;
import kr.co.programmers.collabond.core.auth.jwt.TokenService;
import kr.co.programmers.collabond.util.WithCustomMockUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = ProfileController.class,
        excludeAutoConfiguration = ErrorMvcAutoConfiguration.class
)
@TestPropertySource(properties = {
        "server.port=0",
        "server.address=localhost"
})
@AutoConfigureMockMvc(addFilters = false)
class ProfileControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    ProfileService profileService;

    @MockitoBean
    TokenService tokenService;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    @WithCustomMockUser(username = "testProviderId", role = Role.ROLE_STORE)
    @DisplayName("POST /api/profiles - multipart/form-data 요청에 대해 201 생성 응답")
    void createProfileSuccess_multipart() throws Exception {

        ProfileRequestDto dto = mock(ProfileRequestDto.class);
        when(dto.getUserId()).thenReturn(0L);
        when(dto.getType()).thenReturn("IP");
        when(dto.getName()).thenReturn("새 프로필");
        when(dto.getDescription()).thenReturn("테스트용");
        when(dto.getAddress()).thenReturn("TmpAddress");
        when(dto.getAddressCode()).thenReturn("TmpAddresscode");
        when(dto.getStatus()).thenReturn(true);
        when(dto.getTagIds()).thenReturn(Collections.emptyList());

        ProfileResponseDto mock = mock(ProfileResponseDto.class);
        when(mock.getId()).thenReturn(99L);
        when(mock.getName()).thenReturn("새 프로필");

        when(profileService.create(
                any(ProfileRequestDto.class),
                any(), any(), anyList(), any())
        ).thenReturn(mock);

        MockMultipartFile requestPart = new MockMultipartFile(
                "profileRequest", "", "application/json",
                objectMapper.writeValueAsBytes(dto)
        );
        MockMultipartFile profileImage = new MockMultipartFile(
                "profileImage", "main.jpg", "image/jpeg", "dummy-image".getBytes()
        );
        MockMultipartFile thumbImage = new MockMultipartFile(
                "thumbnailImage", "thumb.jpg", "image/jpeg", "dummy-thumb".getBytes()
        );
        MockMultipartFile extra = new MockMultipartFile(
                "extraImages", "extra1.jpg", "image/jpeg", "dummy-extra".getBytes()
        );

        mockMvc.perform(multipart("/api/profiles")
                        .file(requestPart)
                        .file(profileImage)
                        .file(thumbImage)
                        .file(extra)
                        .with(request -> {
                            request.setMethod("POST");
                            return request;
                        })
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(99L))
                .andExpect(jsonPath("$.name").value("새 프로필"));
    }

    @Test
    @DisplayName("GET /api/profiles/{id} - 존재하지 않는 ID 요청 시 404 반환")
    void getProfileNotFound_returns404() throws Exception {
        when(profileService.findById(123L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/profiles/123"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/profiles/{id} - 성공")
    void getProfileById() throws Exception {

        ProfileResponseDto mock = mock(ProfileResponseDto.class);
        when(mock.getId()).thenReturn(1L);
        when(mock.getName()).thenReturn("새 프로필");

        when(profileService.findById(1L)).thenReturn(Optional.of(mock));

        mockMvc.perform(get("/api/profiles/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @DisplayName("GET /api/profiles/user/{userId} - 성공")
    void getProfilesByUserId() throws Exception {
        ProfileResponseDto mock1 = mock(ProfileResponseDto.class);
        when(mock1.getId()).thenReturn(1L);
        when(mock1.getName()).thenReturn("새 프로필1");

        ProfileResponseDto mock2 = mock(ProfileResponseDto.class);
        when(mock2.getId()).thenReturn(2L);
        when(mock2.getName()).thenReturn("새 프로필2");

        List<ProfileResponseDto> profiles = List.of( mock1, mock2 );

        when(profileService.findAllByUser(1L)).thenReturn(profiles);

        mockMvc.perform(get("/api/profiles/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2));
    }

    @Test
    @DisplayName("DELETE /api/profiles/{id} - 성공")
    void deleteProfile() throws Exception {
        mockMvc.perform(delete("/api/profiles/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("GET /api/profiles/search - 성공")
    void searchProfiles() throws Exception {
        ProfileDetailResponseDto mock = mock(ProfileDetailResponseDto.class);
        when(mock.getId()).thenReturn(1L);
        when(mock.getName()).thenReturn("프로필");

        PageImpl<ProfileDetailResponseDto> result = new PageImpl<>(
                List.of(mock),
                PageRequest.of(0, 10), 1
        );

        when(profileService.searchProfiles(
                eq(ProfileType.STORE),
                any(),
                any(),
                any(Pageable.class))
        ).thenReturn(result);

        mockMvc.perform(get("/api/profiles/search")
                        .param("type", "STORE")
                        .param("tagIds", "1", "2")
                        .param("addressCodes", "11000", "22000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("프로필"));
    }
}