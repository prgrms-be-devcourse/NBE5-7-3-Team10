package kr.co.programmers.collabond.api.profile.interfaces;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.programmers.collabond.api.profile.application.ProfileService;
import kr.co.programmers.collabond.api.profile.domain.ProfileType;
import kr.co.programmers.collabond.api.profile.domain.dto.ProfileDetailResponseDto;
import kr.co.programmers.collabond.api.profile.domain.dto.ProfileRequestDto;
import kr.co.programmers.collabond.api.profile.domain.dto.ProfileResponseDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(ProfileController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = {
        "server.port=0",
        "server.address=localhost"
})
class ProfileControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    ProfileService profileService;

    @MockitoBean
    kr.co.programmers.collabond.core.auth.jwt.TokenAuthenticationFilter tokenAuthenticationFilter;


    @Autowired
    ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /api/profiles - multipart/form-data 요청에 대해 201 생성 응답")
    @WithMockUser
    void createProfileSuccess_multipart() throws Exception {
        ProfileRequestDto dto = ProfileRequestDto.builder()
                .name("새 프로필")
                .description("테스트용")
                .build();

        ProfileResponseDto mock = ProfileResponseDto.builder()
                .id(99L)
                .name("새 프로필")
                .build();

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
                        .with(request -> { request.setMethod("POST"); return request; })
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(99L))
                .andExpect(jsonPath("$.name").value("새 프로필"));
    }

//    @Test
//    @DisplayName("PATCH /api/profiles/{id} - multipart/form-data 수정 요청 정상 처리")
//    void updateProfileSuccess_multipart() throws Exception {
//        ProfileRequestDto dto = ProfileRequestDto.builder()
//                .name("수정된 프로필")
//                .description("설명 수정")
//                .build();
//
//        ProfileResponseDto mock = ProfileResponseDto.builder()
//                .id(5L)
//                .name("수정된 프로필")
//                .build();
//
//        when(profileService.update(
//                eq(5L),
//                any(ProfileRequestDto.class),
//                any(MultipartFile.class),
//                any(MultipartFile.class),
//                anyList()
//        )).thenReturn(mock);
//
//        MockMultipartFile requestPart = new MockMultipartFile(
//                "profileRequest", "", "application/json", objectMapper.writeValueAsBytes(dto)
//        );
//        MockMultipartFile profileImage = new MockMultipartFile(
//                "profileImage", "main.jpg", "image/jpeg", "dummy-image".getBytes()
//        );
//        MockMultipartFile thumbImage = new MockMultipartFile(
//                "thumbnailImage", "thumb.jpg", "image/jpeg", "dummy-thumb".getBytes()
//        );
//
//        MvcResult result = mockMvc.perform(multipart("/api/profiles/5")
//                        .file(requestPart)
//                        .file(profileImage)
//                        .file(thumbImage)
//                        .with(req -> { req.setMethod("PATCH"); return req; })
//                        .contentType(MediaType.MULTIPART_FORM_DATA)
//                        .accept(MediaType.APPLICATION_JSON)
//                )
//                .andExpect(status().isOk())
//                .andReturn();
//
//        MockHttpServletResponse resp = result.getResponse();
//        System.out.println("▶ response status: " + resp.getStatus());
//        System.out.println("▶ response contentType: " + resp.getContentType());
//        System.out.println("▶ response body: '" + resp.getContentAsString() + "'");
//
//        mockMvc.perform(multipart("/api/profiles/5")
//                        .file(requestPart)
//                        .file(profileImage)
//                        .file(thumbImage)
//                        .with(r -> { r.setMethod("PATCH"); return r; })
//                        .contentType(MediaType.MULTIPART_FORM_DATA)
//                        .accept(MediaType.APPLICATION_JSON)
//                )
//                .andExpect(status().isOk())
//                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
//                .andExpect(jsonPath("$.id").value(5L))
//                .andExpect(jsonPath("$.name").value("수정된 프로필"));
//    }


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
        ProfileResponseDto mockResponse = ProfileResponseDto.builder()
                .id(1L)
                .name("테스트")
                .build();

        when(profileService.findById(1L)).thenReturn(Optional.of(mockResponse));

        mockMvc.perform(get("/api/profiles/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));

    }

    @Test
    @DisplayName("GET /api/profiles/user/{userId} - 성공")
    void getProfilesByUserId() throws Exception {
        List<ProfileResponseDto> profiles = List.of(
                ProfileResponseDto.builder().id(1L).name("프로필1").build(),
                ProfileResponseDto.builder().id(2L).name("프로필2").build()
        );

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
        PageImpl<ProfileDetailResponseDto> result = new PageImpl<>(
                List.of(ProfileDetailResponseDto.builder().id(1L).name("SearchProfile").build()),
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
                .andExpect(jsonPath("$.content[0].name").value("SearchProfile"));
    }
}