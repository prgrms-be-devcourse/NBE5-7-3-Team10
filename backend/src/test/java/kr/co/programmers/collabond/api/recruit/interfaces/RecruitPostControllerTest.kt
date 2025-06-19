package kr.co.programmers.collabond.api.recruit.interfaces

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.mockk.*
import kr.co.programmers.collabond.api.profile.domain.dto.ProfileSimpleResponseDto
import kr.co.programmers.collabond.api.recruit.application.RecruitPostService
import kr.co.programmers.collabond.api.recruit.domain.RecruitPostStatus
import kr.co.programmers.collabond.api.recruit.domain.dto.Requests
import kr.co.programmers.collabond.api.recruit.domain.dto.Responses
import kr.co.programmers.collabond.api.user.domain.Role
import kr.co.programmers.collabond.core.auth.oauth2.OAuth2UserInfo
import kr.co.programmers.collabond.shared.exception.ApiExceptionHandler
import kr.co.programmers.collabond.shared.exception.ForbiddenException
import kr.co.programmers.collabond.shared.exception.NotFoundException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.web.PageableHandlerMethodArgumentResolver
import org.springframework.http.MediaType
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.security.authentication.TestingAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.time.LocalDateTime

class RecruitPostControllerTest {

    private lateinit var mockMvc: MockMvc
    private lateinit var objectMapper: ObjectMapper
    private lateinit var recruitPostService: RecruitPostService
    private lateinit var recruitPostController: RecruitPostController
    private lateinit var userInfo: OAuth2UserInfo

    @BeforeEach
    fun setUp() {
        objectMapper = jacksonObjectMapper().apply {
            registerModule(JavaTimeModule())
            registerModule(KotlinModule.Builder().build())
        }

        recruitPostService = mockk()
        recruitPostController = RecruitPostController(recruitPostService)

        userInfo = buildDummyUserInfo()
        SecurityContextHolder.getContext().authentication = TestingAuthenticationToken(userInfo, null)

        val converter = MappingJackson2HttpMessageConverter(objectMapper)

        mockMvc = MockMvcBuilders.standaloneSetup(recruitPostController)
            .setControllerAdvice(ApiExceptionHandler())
            .setMessageConverters(converter)
            .setCustomArgumentResolvers(
                PageableHandlerMethodArgumentResolver(),
                AuthenticationPrincipalArgumentResolver()
            )
            .build()
    }

    private fun buildDummyUserInfo(): OAuth2UserInfo {
        val attributes = mapOf(
            "email" to "test@example.com",
            "providerId" to "1234567890",
            "provider" to "google"
        )

        return OAuth2UserInfo(
            username   = "1234567890",
            email      = "test@example.com",
            _name      = "testuser",
            role       = Role.ROLE_STORE,
            attributes = attributes
        )
    }

    private fun buildDummyProfileDto(): ProfileSimpleResponseDto {
        return ProfileSimpleResponseDto(
            profileId = 10L,
            type = "STORE",
            imageUrl = "https://example.com/image.png",
            address = "서울시 종로구",
            status = true
        )
    }

    private fun buildDummyResponseDto(): Responses {
        return Responses(
            id = 1L,
            title = "테스트 모집글",
            description = "모집글 내용입니다.",
            status = RecruitPostStatus.RECRUITING,
            deadline = LocalDateTime.of(2025, 12, 31, 23, 59),
            profileId = 10L,
            profileName = "짱구",
            profile = buildDummyProfileDto(),
            createdAt = LocalDateTime.of(2025, 6, 1, 12, 0)
        )
    }

    private fun buildUpdatedResponseDto(): Responses {
        return buildDummyResponseDto().copy(
            title = "수정된 제목",
            description = "수정된 설명",
            status = RecruitPostStatus.COMPLETED
        )
    }

    @Test
    @DisplayName("모집글 생성 성공")
    fun `createRecruitPost should return created status when valid request`() {
        val dto = buildDummyResponseDto()
        every { recruitPostService.createRecruitPost(any<Requests>(), any<OAuth2UserInfo>()) } returns dto

        val json = """
            {
              "profileId": 1,
              "title": "테스트 제목",
              "description": "테스트 설명",
              "deadline": "2025-06-23T23:59:59",
              "status": "RECRUITING"
            }
        """.trimIndent()

        mockMvc.perform(
            post("/api/recruitments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .principal(TestingAuthenticationToken(userInfo, null))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(1L))
            .andExpect(jsonPath("$.title").value("테스트 모집글"))
    }

    @Test
    @DisplayName("모집글 단건 조회 성공")
    fun `getRecruitPost should return recruit post when valid id`() {
        every { recruitPostService.getRecruitPostById(1L) } returns buildDummyResponseDto()

        mockMvc.perform(get("/api/recruitments/1"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(1L))
    }

    @Test
    @DisplayName("모집글 단건 조회 실패 - NotFoundException")
    fun `getRecruitPost should return not found when invalid id`() {
        every { recruitPostService.getRecruitPostById(999L) } throws NotFoundException()

        mockMvc.perform(get("/api/recruitments/999"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(404))
    }

    @Test
    @DisplayName("모집글 수정 성공")
    fun `updateRecruitPost should return updated recruit post when valid request`() {
        every {
            recruitPostService.updateRecruitPost(1L, any<Requests>(), any<OAuth2UserInfo>())
        } returns buildUpdatedResponseDto()

        val json = """
            {
              "profileId": 1,
              "title": "수정된 제목",
              "description": "수정된 설명",
              "deadline": "2025-06-23T23:59:59",
              "status": "COMPLETED"
            }
        """.trimIndent()

        mockMvc.perform(
            patch("/api/recruitments/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .principal(TestingAuthenticationToken(userInfo, null))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.title").value("수정된 제목"))
    }

    @Test
    @DisplayName("모집글 수정 실패 - ForbiddenException")
    fun `updateRecruitPost should return forbidden when user is not owner`() {
        every {
            recruitPostService.updateRecruitPost(1L, any<Requests>(), any<OAuth2UserInfo>())
        } throws ForbiddenException()

        val json = """
            {
              "profileId": 1,
              "title": "수정된 제목",
              "description": "수정된 설명",
              "deadline": "2025-06-23T23:59:59",
              "status": "COMPLETED"
            }
        """.trimIndent()

        mockMvc.perform(
            patch("/api/recruitments/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .principal(TestingAuthenticationToken(userInfo, null))
        )
            .andExpect(status().isForbidden)
    }

    @Test
    @DisplayName("모집글 삭제 성공")
    fun `deleteRecruitPost should return ok when valid request`() {
        every { recruitPostService.deleteRecruitPost(1L, any<OAuth2UserInfo>()) } just Runs

        mockMvc.perform(
            delete("/api/recruitments/1")
                .principal(TestingAuthenticationToken(userInfo, null))
        )
            .andExpect(status().isOk)
    }

    @Test
    @DisplayName("모집글 삭제 실패 - ForbiddenException")
    fun `deleteRecruitPost should return forbidden when user is not owner`() {
        every { recruitPostService.deleteRecruitPost(1L, any<OAuth2UserInfo>()) } throws ForbiddenException()

        mockMvc.perform(
            delete("/api/recruitments/1")
                .principal(TestingAuthenticationToken(userInfo, null))
        )
            .andExpect(status().isForbidden)
    }

    @Test
    @DisplayName("모집글 목록 조회 성공")
    fun `getAllRecruitPosts should return recruit posts when valid parameters`() {
        val dto = buildDummyResponseDto()
        val page = PageImpl(listOf(dto), PageRequest.of(0, 10), 1)
        every {
            recruitPostService.getAllRecruitPosts(RecruitPostStatus.RECRUITING, "createdAt", any())
        } returns page

        mockMvc.perform(
            get("/api/recruitments")
                .param("status", "RECRUITING")
                .param("sort", "createdAt")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content[0].id").value(1L))
    }

    @Test
    @DisplayName("파라미터 없이 모집글 목록 조회 성공")
    fun `getAllRecruitPosts should return recruit posts when no parameters`() {
        val dto = buildDummyResponseDto()
        val page = PageImpl(listOf(dto), PageRequest.of(0, 10), 1)
        every { recruitPostService.getAllRecruitPosts(null, null, any()) } returns page

        mockMvc.perform(get("/api/recruitments"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content[0].id").value(1L))
    }
}
