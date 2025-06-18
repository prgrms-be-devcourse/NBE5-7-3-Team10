package kr.co.programmers.collabond.api.apply.application

import kr.co.programmers.collabond.api.apply.domain.ApplyPost
import kr.co.programmers.collabond.api.apply.domain.ApplyPostStatus
import kr.co.programmers.collabond.api.apply.domain.dto.ApplyPostDto
import kr.co.programmers.collabond.api.apply.domain.dto.ApplyPostRequestDto
import kr.co.programmers.collabond.api.apply.domain.dto.ReceivedApplyPostsRequestDto
import kr.co.programmers.collabond.api.apply.domain.dto.SentApplyPostsRequestDto
import kr.co.programmers.collabond.api.apply.infrastructure.ApplyPostRepository
import kr.co.programmers.collabond.api.attachment.domain.Attachment
import kr.co.programmers.collabond.api.file.application.FileService
import kr.co.programmers.collabond.api.file.domain.File
import kr.co.programmers.collabond.api.mail.service.MailService
import kr.co.programmers.collabond.api.profile.application.ProfileService
import kr.co.programmers.collabond.api.profile.domain.Profile
import kr.co.programmers.collabond.api.profile.domain.ProfileType
import kr.co.programmers.collabond.api.recruit.application.RecruitPostService
import kr.co.programmers.collabond.api.recruit.domain.RecruitPost
import kr.co.programmers.collabond.api.recruit.domain.RecruitPostStatus
import kr.co.programmers.collabond.api.user.application.UserService
import kr.co.programmers.collabond.api.user.domain.User
import kr.co.programmers.collabond.core.auth.oauth2.OAuth2UserInfo
import kr.co.programmers.collabond.shared.exception.ErrorCode
import kr.co.programmers.collabond.shared.exception.ForbiddenException
import kr.co.programmers.collabond.shared.exception.InvalidException
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.invocation.InvocationOnMock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.stubbing.Answer
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime
import java.util.*

@ExtendWith(MockitoExtension::class)
internal class ApplyPostServiceTest {
    @Mock
    lateinit var recruitPostService: RecruitPostService

    @Mock
    lateinit var profileService: ProfileService

    @Mock
    lateinit var userService: UserService

    @Mock
    lateinit var fileService: FileService

    @Mock
    lateinit var mailService: MailService

    @Mock
    lateinit var applyPostRepository: ApplyPostRepository

    @InjectMocks
    lateinit var applyPostService: ApplyPostService

    @Test
    @DisplayName("지원서 제출 테스트")
    fun applyPost_success() {
        // given
        val recruitmentId = 1L
        val profileId = 2L
        val userId = 3L
        val recruitProfileName = "recruitProfileName"
        val recruitTitle = "recruitTitle"
        val recruitDescription = "recruitDescription"
        val deadline = LocalDateTime.now()
        val profileName = "profileName"
        val profileDescription = "profileDescription"


        val request = ApplyPostRequestDto(profileId, "내용")
        val recruitPost = mock(RecruitPost::class.java)
        val profile = mock(Profile::class.java)
        val recruitProfile = mock(Profile::class.java)
        val user = mock(User::class.java)
        val applyPost = mock(ApplyPost::class.java)

        // Setup profile types
        `when`<Profile?>(recruitPost.profile).thenReturn(recruitProfile)
        `when`(recruitPost.title).thenReturn(recruitTitle)
        `when`(recruitPost.description).thenReturn(recruitDescription)
        `when`(recruitPost.deadline).thenReturn(deadline)
        `when`(recruitPost.status).thenReturn(RecruitPostStatus.RECRUITING)
        `when`(recruitProfile.type).thenReturn(ProfileType.STORE)
        `when`(recruitProfile.name).thenReturn(recruitProfileName)
        `when`(profile.name).thenReturn(profileName)
        `when`(profile.description).thenReturn(profileDescription)
        `when`(profile.type).thenReturn(ProfileType.IP)
        `when`(profile.user).thenReturn(user)
        `when`(user.id).thenReturn(userId)

        `when`(recruitPostService.findByRecruitmentId(recruitmentId))
            .thenReturn(recruitPost)
        `when`(profileService.findByProfileId(profileId)).thenReturn(profile)

        `when`(
            applyPostRepository.save(any(ApplyPost::class.java))
        ).thenAnswer(
            Answer { invocation: InvocationOnMock? ->
                invocation!!.getArgument<ApplyPost?>(0)
                `when`(applyPost.id).thenReturn(1L)
                `when`(applyPost.recruitPost).thenReturn(recruitPost)
                `when`(applyPost.profile).thenReturn(profile)
                `when`(applyPost.content).thenReturn(request.content)
                `when`(applyPost.status).thenReturn(ApplyPostStatus.PENDING)
                `when`<MutableList<Attachment>?>(applyPost.attachments).thenReturn(null)
                `when`(applyPost.createdAt).thenReturn(LocalDateTime.now())
                applyPost
            })

        // when
        val result = applyPostService.applyPost(recruitmentId, request, null)

        // then
        Assertions.assertNotNull(result)
        verify(applyPostRepository).save(any(ApplyPost::class.java))
        verify(mailService).sendReceivedApplyMail(
            eq(recruitPost),
            eq(profile),
            any()
        )
    }

    @Test
    @DisplayName("파일이 있는 지원서 제출 테스트")
    fun applyPost_withFiles_success() {
        // given
        val recruitmentId = 1L
        val profileId = 2L
        val userId = 3L
        val recruitProfileName = "recruitProfileName"
        val recruitTitle = "recruitTitle"
        val recruitDescription = "recruitDescription"
        val deadline = LocalDateTime.now()
        val profileName = "profileName"
        val profileDescription = "profileDescription"

        val request = ApplyPostRequestDto(profileId, "내용")
        val recruitPost = mock(RecruitPost::class.java)
        val profile = mock(Profile::class.java)
        val recruitProfile = mock(Profile::class.java)
        val user = mock(User::class.java)
        val applyPost = mock(ApplyPost::class.java)

        val files: MutableList<MultipartFile> = ArrayList<MultipartFile>()
        files.add(mock(MultipartFile::class.java))
        val savedFiles: MutableList<File> = ArrayList<File>()
        val savedFile = mock(File::class.java)
        savedFiles.add(savedFile)

        val attachments: MutableList<Attachment> = ArrayList<Attachment>()
        val attachment = mock(Attachment::class.java)
        attachments.add(attachment)

        // Setup profile types
        `when`<Profile?>(recruitPost.profile).thenReturn(recruitProfile)
        `when`(recruitPost.title).thenReturn(recruitTitle)
        `when`(recruitPost.description).thenReturn(recruitDescription)
        `when`(recruitPost.deadline).thenReturn(deadline)
        `when`(recruitPost.status).thenReturn(RecruitPostStatus.RECRUITING)
        `when`(recruitProfile.type).thenReturn(ProfileType.STORE)
        `when`(recruitProfile.name).thenReturn(recruitProfileName)
        `when`(profile.name).thenReturn(profileName)
        `when`(profile.description).thenReturn(profileDescription)
        `when`(profile.type).thenReturn(ProfileType.IP)
        `when`(profile.user).thenReturn(user)
        `when`(user.id).thenReturn(userId)

        // Setup file mocks
        `when`(savedFile.originName).thenReturn("originalName.pdf")
        `when`(savedFile.savedName).thenReturn("savedName.pdf")
        `when`(attachment.file).thenReturn(savedFile)

        // Setup service mocks
        `when`(recruitPostService.findByRecruitmentId(recruitmentId))
            .thenReturn(recruitPost)
        `when`(profileService.findByProfileId(profileId)).thenReturn(profile)
        `when`(fileService.saveFiles(files)).thenReturn(savedFiles)

        `when`(
            applyPostRepository.save(any(ApplyPost::class.java))
        ).thenAnswer(
            Answer { invocation: InvocationOnMock? ->
                invocation!!.getArgument<ApplyPost?>(0)
                `when`(applyPost.id).thenReturn(1L)
                `when`(applyPost.recruitPost).thenReturn(recruitPost)
                `when`(applyPost.profile).thenReturn(profile)
                `when`(applyPost.content).thenReturn(request.content)
                `when`(applyPost.status)
                    .thenReturn(ApplyPostStatus.PENDING)
                `when`<MutableList<Attachment>?>(applyPost.attachments)
                    .thenReturn(attachments)
                `when`(applyPost.createdAt).thenReturn(LocalDateTime.now())
                applyPost
            })

        // when
        val result = applyPostService.applyPost(recruitmentId, request, files)

        // then
        Assertions.assertNotNull(result)
        verify(fileService).saveFiles(files)
        verify(applyPostRepository)
            .save(any(ApplyPost::class.java))
        verify(mailService).sendReceivedApplyMail(
            eq(recruitPost),
            eq(profile),
            any()
        )
    }

    @Test
    @DisplayName("같은 타입의 프로필로 지원서를 제출하면 InvalidException을 발생시킨다")
    fun applyPost_withSameProfileType_fail() {
        // given
        val recruitmentId = 1L
        val profileId = 2L
        val request = ApplyPostRequestDto(profileId, "내용")
        val recruitPost = mock(RecruitPost::class.java)
        val recruitProfile = mock(Profile::class.java)
        val applyProfile = mock(Profile::class.java)

        `when`<Profile?>(recruitPost.profile).thenReturn(recruitProfile)
        `when`(recruitProfile.type).thenReturn(ProfileType.STORE)
        `when`(applyProfile.type).thenReturn(ProfileType.STORE)
        `when`(recruitPostService.findByRecruitmentId(recruitmentId))
            .thenReturn(recruitPost)
        `when`(profileService.findByProfileId(profileId))
            .thenReturn(applyProfile)

        // when & then
        val exception =
            Assertions.assertThrows(InvalidException::class.java) {
                applyPostService.applyPost(recruitmentId, request, null)
            }
        Assertions.assertEquals(ErrorCode.REQUEST_TO_SAME_ROLE.message, exception.message)
    }

    @Test
    @DisplayName("status 없이 보낸 지원서를 호출하면 모든 보낸 지원서를 불러온다")
    fun findSentApplyPosts_withoutStatus() {
        // given
        val request = SentApplyPostsRequestDto()
        val userInfo = mock(OAuth2UserInfo::class.java)
        val user = mock(User::class.java)
        val pageable = mock(Pageable::class.java)
        val expectedPage =
            PageImpl(listOf<ApplyPostDto>(mock(ApplyPostDto::class.java)))

        `when`(userInfo.username).thenReturn("username")
        `when`(userService.findByProviderId("username")).thenReturn(user)
        `when`(user.id).thenReturn(1L)
        `when`(applyPostRepository.findAllSentByUser(1L, pageable))
            .thenReturn(expectedPage)

        // when
        val result: Page<ApplyPostDto> =
            applyPostService.findSentApplyPosts(request, userInfo, pageable)

        // then
        Assertions.assertNotNull(result)
        Assertions.assertEquals(expectedPage, result)
        verify(applyPostRepository).findAllSentByUser(1L, pageable)
        verify(applyPostRepository, never())
            .findAllSentByUserIdAndStatus(
                anyLong(),
                anyString(),
                any<Pageable>() ?: pageable,
            )
    }

    @Test
    @DisplayName("status와 함께 보낸 지원서를 호출하면 필터링된 보낸 지원서를 불러온다")
    fun findSentApplyPosts_withStatus() {
        // given
        val request = mock(SentApplyPostsRequestDto::class.java)
        val userInfo = mock(OAuth2UserInfo::class.java)
        val user = mock(User::class.java)
        val pageable = mock(Pageable::class.java)
        val expectedPage =
            PageImpl(listOf<ApplyPostDto>(mock(ApplyPostDto::class.java)))

        `when`<String?>(request.status).thenReturn("PENDING")
        `when`(userInfo.username).thenReturn("username")
        `when`(userService.findByProviderId("username")).thenReturn(user)
        `when`(user.id).thenReturn(1L)
        `when`(
            applyPostRepository.findAllSentByUserIdAndStatus(
                1L,
                "PENDING",
                pageable
            )
        ).thenReturn(expectedPage)

        // when
        val result: Page<ApplyPostDto> =
            applyPostService.findSentApplyPosts(request, userInfo, pageable)

        // then
        Assertions.assertNotNull(result)
        Assertions.assertEquals(expectedPage, result)
        verify(applyPostRepository)
            .findAllSentByUserIdAndStatus(1L, "PENDING", pageable)
        verify(applyPostRepository, never())
            .findAllSentByUser(
                anyLong(),
                any<Pageable>() ?: pageable,
            )
    }

    @Test
    @DisplayName("status 없이 받은 지원서를 호출하면 모든 받은 지원서를 불러온다")
    fun findReceivedApplyPosts_withoutStatus() {
        // given
        val request = ReceivedApplyPostsRequestDto()
        val userInfo = mock(OAuth2UserInfo::class.java)
        val user = mock(User::class.java)
        val pageable = mock(Pageable::class.java)
        val expectedPage =
            PageImpl(listOf<ApplyPostDto>(mock(ApplyPostDto::class.java)))

        `when`(userInfo.username).thenReturn("username")
        `when`(userService.findByProviderId("username")).thenReturn(user)
        `when`(user.id).thenReturn(1L)
        `when`(
            applyPostRepository.findAllReceivedByUser(
                1L,
                pageable
            )
        ).thenReturn(expectedPage)

        // when
        val result: Page<ApplyPostDto> =
            applyPostService.findReceivedApplyPosts(request, userInfo, pageable)

        // then
        Assertions.assertNotNull(result)
        Assertions.assertEquals(expectedPage, result)
        verify(applyPostRepository)
            .findAllReceivedByUser(1L, pageable)
        verify(applyPostRepository, never())
            .findAllReceivedByUserIdAndStatus(
                anyLong(),
                anyString(),
                any<Pageable>() ?: pageable,
            )
    }

    @Test
    @DisplayName("status와 함께 받은 지원서를 호출하면 필터링된 받은 지원서를 불러온다")
    fun findReceivedApplyPosts_withStatus() {
        // given
        val request =
            mock(ReceivedApplyPostsRequestDto::class.java)
        val userInfo = mock(OAuth2UserInfo::class.java)
        val user = mock(User::class.java)
        val pageable = mock(Pageable::class.java)
        val expectedPage =
            PageImpl(listOf<ApplyPostDto>(mock(ApplyPostDto::class.java)))

        `when`<String?>(request.status).thenReturn("PENDING")
        `when`(userInfo.username).thenReturn("username")
        `when`(userService.findByProviderId("username")).thenReturn(user)
        `when`(user.id).thenReturn(1L)
        `when`(
            applyPostRepository.findAllReceivedByUserIdAndStatus(
                1L,
                "PENDING",
                pageable
            )
        ).thenReturn(expectedPage)

        // when
        val result: Page<ApplyPostDto> =
            applyPostService.findReceivedApplyPosts(request, userInfo, pageable)

        // then
        Assertions.assertNotNull(result)
        Assertions.assertEquals(expectedPage, result)
        verify(applyPostRepository)
            .findAllReceivedByUserIdAndStatus(1L, "PENDING", pageable)
        verify(applyPostRepository, never())
            .findAllReceivedByUser(
                anyLong(),
                any<Pageable>() ?: pageable
            )

    }

    @Test
    @DisplayName("지원서 수락은 상태를 업데이트하고 메일을 전송한다")
    fun acceptApply_success() {
        // given
        val applicationId = 1L
        val userInfo = mock(OAuth2UserInfo::class.java)
        val user = mock(User::class.java)
        val applyPost = mock(ApplyPost::class.java)
        val recruitPost = mock(RecruitPost::class.java)
        val recruitProfile = mock(Profile::class.java)
        val applyProfile = mock(Profile::class.java)

        `when`(userInfo.username).thenReturn("username")
        `when`(userService.findByProviderId("username")).thenReturn(user)
        `when`(user.id).thenReturn(1L)
        `when`(applyPostRepository.findById(applicationId))
            .thenReturn(Optional.of(applyPost))
        `when`(applyPost.recruitPost).thenReturn(recruitPost)
        `when`<Profile?>(recruitPost.profile).thenReturn(recruitProfile)
        `when`(recruitProfile.user).thenReturn(user)
        `when`(applyPost.profile).thenReturn(applyProfile)

        // when
        applyPostService.acceptApply(applicationId, userInfo)

        // then
        verify(applyPost).updateStatus(ApplyPostStatus.ACCEPTED)
        verify(applyProfile).updateCollaboCount()
        verify(recruitProfile).updateCollaboCount()
        verify(mailService).sendBondCompletionEmails(
            eq(applyPost),
            any(LocalDateTime::class.java)
        )
    }

    @Test
    @DisplayName("존재하지 않는 지원서를 수락하면 InvalidException을 발생시킨다")
    fun acceptApply_withNonExistingApplication_fail() {
        // given
        val applicationId = 1L
        val userInfo = mock(OAuth2UserInfo::class.java)

        `when`(applyPostRepository.findById(applicationId))
            .thenReturn(Optional.empty())

        // when & then
        val exception =
            Assertions.assertThrows(InvalidException::class.java) {
                applyPostService.acceptApply(applicationId, userInfo)
            }
        Assertions.assertEquals(
            ErrorCode.APPLY_NOT_FOUND.message,
            exception.message
        )
    }

    @Test
    @DisplayName("로그인된 프로필이 공고 작성자가 아닌상태에서 수락하려한다면 ForbiddenException을 발생시킨다")
    fun acceptApply_withUnauthorizedUser_fail() {
        // given
        val applicationId = 1L
        val userInfo = mock(OAuth2UserInfo::class.java)
        val loginUser = mock(User::class.java)
        val recruitUser = mock(User::class.java)
        val applyPost = mock(ApplyPost::class.java)
        val recruitPost = mock(RecruitPost::class.java)
        val recruitProfile = mock(Profile::class.java)

        `when`(userInfo.username).thenReturn("username")
        `when`(userService.findByProviderId("username")).thenReturn(loginUser)
        `when`(loginUser.id).thenReturn(1L)
        `when`(applyPostRepository.findById(applicationId))
            .thenReturn(Optional.of(applyPost))
        `when`(applyPost.recruitPost).thenReturn(recruitPost)
        `when`<Profile?>(recruitPost.profile).thenReturn(recruitProfile)
        `when`(recruitProfile.user).thenReturn(recruitUser)
        `when`(recruitUser.id).thenReturn(2L)

        // when & then
        val exception =
            Assertions.assertThrows(ForbiddenException::class.java) {
                applyPostService.acceptApply(applicationId, userInfo)
            }
        Assertions.assertEquals(
            ErrorCode.FORBIDDEN_REQUEST.message,
            exception.message
        )
    }
}
