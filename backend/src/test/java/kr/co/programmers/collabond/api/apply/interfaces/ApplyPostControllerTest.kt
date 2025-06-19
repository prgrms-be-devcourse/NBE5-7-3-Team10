package kr.co.programmers.collabond.api.apply.interfaces

import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.every
import io.mockk.mockk
import kr.co.programmers.collabond.api.apply.application.ApplyPostService
import kr.co.programmers.collabond.api.apply.domain.dto.ApplyPostDto
import kr.co.programmers.collabond.api.apply.domain.dto.ApplyPostRequestDto
import kr.co.programmers.collabond.api.apply.domain.dto.ReceivedApplyPostsRequestDto
import kr.co.programmers.collabond.api.apply.domain.dto.SentApplyPostsRequestDto
import kr.co.programmers.collabond.api.profile.domain.dto.ProfileDto
import kr.co.programmers.collabond.api.recruit.domain.RecruitPostStatus
import kr.co.programmers.collabond.api.recruit.domain.dto.RecruitPostDto
import kr.co.programmers.collabond.core.auth.jwt.TokenAuthenticationFilter
import kr.co.programmers.collabond.core.config.SecurityConfig
import kr.co.programmers.collabond.shared.exception.ErrorCode
import kr.co.programmers.collabond.shared.exception.InvalidException
import kr.co.programmers.collabond.util.WithCustomMockUser
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.multipart
import org.springframework.test.web.servlet.patch
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.function.RequestPredicates.contentType
import java.time.LocalDateTime

@WebMvcTest(
    controllers = [ApplyPostController::class],
    excludeFilters = [ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = [SecurityConfig::class, TokenAuthenticationFilter::class]
    )]
)
@AutoConfigureMockMvc(addFilters = false)
@Import(ApplyPostControllerTest.MockConfig::class) // <- MockK 주입용 설정
class ApplyPostControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Autowired
    lateinit var applyPostService: ApplyPostService // MockK로 주입

    @Test
    @DisplayName("지원서 제출 테스트")
    @WithCustomMockUser
    fun `applyPost_success`() {
        // Given
        val recruitmentId = 1L
        val profileId = 2L
        val applyPostId = 3L
        val content = "지원 내용입니다"

        val requestDto = ApplyPostRequestDto(profileId, content)
        val requestJson = objectMapper.writeValueAsString(requestDto)

        val jsonFile = MockMultipartFile(
            "applyRequest",
            "",
            "application/json",
            requestJson.toByteArray(Charsets.UTF_8),
        )

        val attachment = MockMultipartFile(
            "attachment",
            "test.pdf",
            "application/pdf",
            "test content".toByteArray(Charsets.UTF_8)
        )

        val responseDto = ApplyPostDto(
            id = applyPostId,
            content = content,
            status = "PENDING",
            recruitPost = buildDummyRecruitPostDto(),
            profile = buildDummyProfileDto(),
            attachmentFiles = null,
            createdAt = LocalDateTime.now(),
        )

        every {
            applyPostService.applyPost(
                any<Long>(), any<ApplyPostRequestDto>(), any<List<MultipartFile>>()
            )
        } returns responseDto

        // When & Then
        mockMvc.multipart("/api/applications/{recruitmentId}", recruitmentId) {
            file(jsonFile)
            file(attachment)
            with(csrf())
            contentType(MediaType.MULTIPART_FORM_DATA)
        }.andExpect {
            status { isCreated() }
            jsonPath("$.id") { value(responseDto.id) }
            jsonPath("$.content") { value(responseDto.content) }
            jsonPath("$.status") { value(responseDto.status) }
        }.andDo { print() }
    }

    @Test
    @DisplayName("지원서 제출 실패 테스트")
    @WithCustomMockUser
    fun `applyPost_fail`() {
        // Given
        val recruitmentId = 1L
        val profileId = 2L
        val content = "지원 내용입니다"

        val requestDto = ApplyPostRequestDto(profileId, content)
        val requestJson = objectMapper.writeValueAsString(requestDto)

        val jsonFile = MockMultipartFile(
            "applyRequest",
            "",
            "application/json",
            requestJson.toByteArray(Charsets.UTF_8),
        )

        val attachment = MockMultipartFile(
            "attachment",
            "test.pdf",
            "application/pdf",
            "test content".toByteArray(Charsets.UTF_8)
        )

        every {
            applyPostService.applyPost(
                any<Long>(), any<ApplyPostRequestDto>(), any<List<MultipartFile>>()
            )
        } throws InvalidException(ErrorCode.REQUEST_TO_SAME_ROLE)

        // When & Then
        mockMvc.multipart("/api/applications/$recruitmentId") {
            file(jsonFile)
            file(attachment)
            with(csrf())
            contentType(MediaType.MULTIPART_FORM_DATA)
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.message") { value(ErrorCode.REQUEST_TO_SAME_ROLE.message) }
        }.andDo { print() }
    }

    @Test
    @DisplayName("보낸 지원서 목록 조회 테스트")
    @WithCustomMockUser
    fun `sentApplyPost`() {

        val applyPost1 = buildDummyApplyPostDto(1L, "PENDING")
        val applyPost2 = buildDummyApplyPostDto(2L, "ACCEPTED")
        val applyPosts = listOf(applyPost1, applyPost2)

        val page = PageImpl(applyPosts, PageRequest.of(1, 10), 1)

        every {
            applyPostService.findSentApplyPosts(
                any<SentApplyPostsRequestDto>(),
                any(),
                any<Pageable>()
            )
        } returns page

        mockMvc.get("/api/applications/sent") {
            with(csrf())
            contentType(MediaType.APPLICATION_JSON)
        }.andExpect {
            status { isOk() }
            jsonPath("$.content") { isArray() }
            jsonPath("$.content.length()") { value(2) }
            jsonPath("$.content[0].id") { value(1) }
            jsonPath("$.content[0].status") { value("PENDING") }
            jsonPath("$.content[1].id") { value(2) }
            jsonPath("$.content[1].status") { value("ACCEPTED") }
        }.andDo { print() }
    }

    @Test
    @DisplayName("받은 지원서 목록 조회 테스트")
    @WithCustomMockUser
    fun `receivedApplyPosts`() {

        val applyPost1 = buildDummyApplyPostDto(1L, "PENDING")
        val applyPost2 = buildDummyApplyPostDto(2L, "ACCEPTED")
        val applyPosts = listOf(applyPost1, applyPost2)

        val page = PageImpl(applyPosts, PageRequest.of(1, 10), 1)

        every {
            applyPostService.findReceivedApplyPosts(
                any<ReceivedApplyPostsRequestDto>(),
                any(),
                any<Pageable>()
            )
        } returns page

        mockMvc.get("/api/applications/received") {
            with(csrf())
            contentType(MediaType.APPLICATION_JSON)
        }.andExpect {
            status { isOk() }
            jsonPath("$.content") { isArray() }
            jsonPath("$.content.length()") { value(2) }
            jsonPath("$.content[0].id") { value(1) }
            jsonPath("$.content[0].status") { value("PENDING") }
            jsonPath("$.content[1].id") { value(2) }
            jsonPath("$.content[1].status") { value("ACCEPTED") }
        }.andDo { print() }
    }

    @Test
    @DisplayName("지원서 수락 테스트")
    @WithCustomMockUser
    fun `acceptApply`() {
        // given
        val applicationId = 1L

        // when & then
        mockMvc.patch("/api/applications/accept/$applicationId") {
            with(csrf())
            contentType(MediaType.APPLICATION_JSON)
        }.andExpect {
            status { isOk() }
        }.andDo { print() }
    }

    @TestConfiguration
    class MockConfig {
        @Bean
        fun applyPostService(): ApplyPostService = mockk(relaxed = true)
    }

    private fun buildDummyApplyPostDto(
        id: Long,
        status: String
    ): ApplyPostDto = ApplyPostDto(
        id = id,
        status = status,
        recruitPost = buildDummyRecruitPostDto(),
        profile = buildDummyProfileDto(),
        content = "this is content",
        attachmentFiles = null,
        createdAt = LocalDateTime.now(),
    )

    private fun buildDummyProfileDto(): ProfileDto {
        return ProfileDto(
            id = 2L,
            name = "dummy profile",
            description = "this is dummy profile",
            type = "STORE",
            imageUrl = "https://example.com/image.png",
            collaboCount = 3,
            userId = 11L
        )
    }

    private fun buildDummyRecruitPostDto(): RecruitPostDto {
        return RecruitPostDto(
            id = 1L,
            title = "테스트 모집글",
            description = "모집글 내용입니다.",
            status = RecruitPostStatus.RECRUITING.name,
            deadline = LocalDateTime.of(2025, 12, 31, 23, 59),
            writerProfileId = 10L,
            writerProfileName = "짱구",
        )
    }
}
