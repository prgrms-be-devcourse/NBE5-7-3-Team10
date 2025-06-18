package kr.co.programmers.collabond.api.profile.interfaces

import com.fasterxml.jackson.databind.ObjectMapper
import kr.co.programmers.collabond.api.profile.application.ProfileService
import kr.co.programmers.collabond.api.profile.domain.ProfileType
import kr.co.programmers.collabond.api.profile.domain.dto.ProfileDetailResponseDto
import kr.co.programmers.collabond.api.profile.domain.dto.ProfileRequestDto
import kr.co.programmers.collabond.api.profile.domain.dto.ProfileResponseDto
import kr.co.programmers.collabond.api.user.domain.Role
import kr.co.programmers.collabond.core.auth.jwt.TokenService
import kr.co.programmers.collabond.core.auth.oauth2.OAuth2UserInfo
import kr.co.programmers.collabond.util.WithCustomMockUser
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.*
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.web.multipart.MultipartFile
import java.util.Optional

@WebMvcTest(
    controllers = [ProfileController::class],
    excludeAutoConfiguration = [ErrorMvcAutoConfiguration::class]
)
@TestPropertySource(properties = ["server.port=0", "server.address=localhost"])
@AutoConfigureMockMvc(addFilters = false)
class ProfileControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var profileService: ProfileService

    @MockitoBean
    private lateinit var tokenService: TokenService

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    @WithCustomMockUser(username = "testProviderId", role = Role.ROLE_STORE)
    @DisplayName("POST /api/profiles - multipart/form-data 요청에 대해 201 생성 응답")
    fun createProfileSuccess_multipart() {

        val dto = ProfileRequestDto(
            userId      = 0L,
            type        = "IP",
            name        = "새 프로필",
            description = "테스트용",
            address     = "TmpAddress",
            addressCode = "TmpAddresscode",
            status      = true,
            tagIds      = emptyList()
        )
        val requestPart = MockMultipartFile(
            "profileRequest", "", "application/json",
            objectMapper.writeValueAsBytes(dto)
        )
        val profileImg = MockMultipartFile(
            "profileImage", "main.jpg", "image/jpeg", "dummy-image".toByteArray()
        )
        val thumbImg = MockMultipartFile(
            "thumbnailImage", "thumb.jpg", "image/jpeg", "dummy-thumb".toByteArray()
        )
        val extraImg = MockMultipartFile(
            "extraImages", "extra1.jpg", "image/jpeg", "dummy-extra".toByteArray()
        )

        mockMvc.perform(
            multipart("/api/profiles")
                .file(requestPart)
                .file(profileImg)
                .file(thumbImg)
                .file(extraImg)
                .with { it.method = "POST"; it }
                .contentType(MediaType.MULTIPART_FORM_DATA)
        ).andExpect(status().isCreated)
            .andExpect(content().string(""))
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(99L))
            .andExpect(jsonPath("$.name").value("새 프로필"))
    }

    @Test
    @DisplayName("GET /api/profiles/{id} - 존재하지 않는 ID 요청 시 404 반환")
    fun profileNotFound_returns404() {
        Mockito.`when`(profileService.findById(123L)).thenReturn(Optional.empty())

        mockMvc.perform(get("/api/profiles/123"))
            .andExpect(status().isNotFound)
    }

    @Test
    @DisplayName("GET /api/profiles/{id} - 성공")
    fun profileById() {
        val responseDto = Mockito.mock(ProfileResponseDto::class.java).apply {
            Mockito.`when`(id).thenReturn(1L)
            Mockito.`when`(name).thenReturn("새 프로필")
        }
        Mockito.`when`(profileService.findById(1L)).thenReturn(Optional.of(responseDto))

        mockMvc.perform(get("/api/profiles/1"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(1L))
    }

    @Test
    @DisplayName("GET /api/profiles/user/{userId} - 성공")
    fun profilesByUserId() {
        val mock1 = Mockito.mock(ProfileResponseDto::class.java).apply {
              Mockito.`when`(id).thenReturn(1L)
        }
        val mock2 = Mockito.mock(ProfileResponseDto::class.java).apply {
            Mockito.`when`(id).thenReturn(2L)
        }
        Mockito.`when`(profileService.findAllByUser(1L)).thenReturn(listOf(mock1, mock2))

        mockMvc.perform(get("/api/profiles/user/1"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content[0].id").value(1L))
    }

    @Test
    @DisplayName("DELETE /api/profiles/{id} - 성공")
    fun deleteProfile() {
        mockMvc.perform(delete("/api/profiles/1"))
            .andExpect(status().isNoContent)
    }

    @Test
    @DisplayName("GET /api/profiles/search - 성공")
    fun searchProfiles() {
        val detailDto = Mockito.mock(ProfileDetailResponseDto::class.java).apply {
            Mockito.`when`(id).thenReturn(1L)
            Mockito.`when`(name).thenReturn("프로필")
        }
        val result = PageImpl(listOf(detailDto), PageRequest.of(0, 10), 1)
        Mockito.doReturn(result)
            .`when`(profileService)
            .searchProfiles(
                eq(ProfileType.STORE),
                anyList<String>(),
                anyList<Long>(),
                any(Pageable::class.java)
            )

        mockMvc.perform(
            get("/api/profiles/search")
                .param("type", "STORE")
                .param("tagIds", "1", "2")
                .param("addressCodes", "11000", "22000")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content[0].id").value(1L))
    }
}
