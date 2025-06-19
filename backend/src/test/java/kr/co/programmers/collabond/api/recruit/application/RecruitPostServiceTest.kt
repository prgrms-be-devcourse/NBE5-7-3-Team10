package kr.co.programmers.collabond.api.recruit.application

import kr.co.programmers.collabond.api.file.domain.File
import kr.co.programmers.collabond.api.image.domain.Image
import kr.co.programmers.collabond.api.profile.application.ProfileService
import kr.co.programmers.collabond.api.profile.domain.Profile
import kr.co.programmers.collabond.api.profile.domain.ProfileType
import kr.co.programmers.collabond.api.recruit.domain.RecruitPost
import kr.co.programmers.collabond.api.recruit.domain.RecruitPostStatus
import kr.co.programmers.collabond.api.recruit.domain.dto.Requests
import kr.co.programmers.collabond.api.recruit.infrastructure.RecruitPostRepository
import kr.co.programmers.collabond.api.user.application.UserService
import kr.co.programmers.collabond.api.user.domain.User
import kr.co.programmers.collabond.core.auth.oauth2.OAuth2UserInfo
import kr.co.programmers.collabond.shared.exception.ForbiddenException
import kr.co.programmers.collabond.shared.exception.InvalidException
import kr.co.programmers.collabond.shared.exception.NotFoundException
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable
import org.mockito.*
import java.time.LocalDateTime
import java.util.*
import java.util.List

internal class RecruitPostServiceTest {
    @Mock
    private val profileService: ProfileService? = null

    @Mock
    private val userService: UserService? = null

    @Mock
    private val recruitPostRepository: RecruitPostRepository? = null

    @InjectMocks
    private val recruitPostService: RecruitPostService? = null

    @Mock
    private val userInfo: OAuth2UserInfo? = null

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
    }

    private fun createRequestDto(): Requests {
        return Requests(
            1L,
            "제목",
            "설명",
            LocalDateTime.now().plusDays(5),
            "RECRUITING"
        )
    }

    private fun mockImageAndType(profile: Profile) {
        val file = Mockito.mock<File>(File::class.java)
        Mockito.`when`<String?>(file.savedName).thenReturn("test.jpg")

        val image = Mockito.mock<Image>(Image::class.java)
        Mockito.`when`<String?>(image.type).thenReturn("PROFILE")
        Mockito.`when`<File?>(image.file).thenReturn(file)

        Mockito.`when`<MutableList<Image>?>(profile.images).thenReturn(List.of<Image>(image))
        Mockito.`when`<ProfileType?>(profile.type).thenReturn(ProfileType.STORE)

        Mockito.`when`<Long?>(profile.id).thenReturn(1L)
        Mockito.`when`<String?>(profile.name).thenReturn("테스트 프로필")
    }

    @Test
    fun 모집글_성공적으로_생성한다() {
        val requestDto = createRequestDto()

        val loginUser = Mockito.mock<User>(User::class.java)
        Mockito.`when`<Long?>(loginUser.id).thenReturn(1L)

        val profileUser = Mockito.mock<User>(User::class.java)
        Mockito.`when`<Long?>(profileUser.id).thenReturn(1L)

        val profile = Mockito.mock<Profile>(Profile::class.java)
        Mockito.`when`<User?>(profile.user).thenReturn(profileUser)
        Mockito.`when`<Long?>(profile.id).thenReturn(1L)
        Mockito.`when`<String?>(profile.name).thenReturn("테스트 프로필")
        mockImageAndType(profile)

        val post = Mockito.mock<RecruitPost>(RecruitPost::class.java)
        Mockito.`when`<Long?>(post.id).thenReturn(1L)
        Mockito.`when`<String?>(post.title).thenReturn("제목")
        Mockito.`when`<String?>(post.description).thenReturn("설명")
        Mockito.`when`<RecruitPostStatus?>(post.status).thenReturn(RecruitPostStatus.RECRUITING)
        Mockito.`when`<LocalDateTime?>(post.deadline).thenReturn(LocalDateTime.now().plusDays(5))
        Mockito.`when`<LocalDateTime?>(post.createdAt).thenReturn(LocalDateTime.now())
        Mockito.`when`<LocalDateTime?>(post.deletedAt).thenReturn(null)
        Mockito.`when`<Profile?>(post.profile).thenReturn(profile)

        Mockito.`when`<String?>(userInfo!!.username).thenReturn("providerId")
        Mockito.`when`<User?>(userService!!.findByProviderId("providerId")).thenReturn(loginUser)
        Mockito.`when`<Profile?>(profileService!!.findByProfileId(1L)).thenReturn(profile)
        Mockito.`when`<Any?>(recruitPostRepository!!.save<RecruitPost>(ArgumentMatchers.any<RecruitPost>()))
            .thenReturn(post)

        val responses = recruitPostService!!.createRecruitPost(requestDto, userInfo)

        Assertions.assertNotNull(responses)
        Mockito.verify<RecruitPostRepository?>(recruitPostRepository)
            .save<RecruitPost>(ArgumentMatchers.any<RecruitPost>())
    }

    @Test
    fun 프로필_작성자가_아닌_경우_ForbiddenException() {
        val requestDto = createRequestDto()

        val loginUser = Mockito.mock<User>(User::class.java)
        Mockito.`when`<Long?>(loginUser.id).thenReturn(1L)

        val otherUser = Mockito.mock<User>(User::class.java)
        Mockito.`when`<Long?>(otherUser.id).thenReturn(2L)

        val profile = Mockito.mock<Profile>(Profile::class.java)
        Mockito.`when`<User?>(profile.user).thenReturn(otherUser)
        mockImageAndType(profile)

        Mockito.`when`<String?>(userInfo!!.username).thenReturn("providerId")
        Mockito.`when`<User?>(userService!!.findByProviderId("providerId")).thenReturn(loginUser)
        Mockito.`when`<Profile?>(profileService!!.findByProfileId(1L)).thenReturn(profile)

        Assertions.assertThrows<ForbiddenException?>(ForbiddenException::class.java, Executable {
            recruitPostService!!.createRecruitPost(requestDto, userInfo)
        })

        Mockito.verify<RecruitPostRepository?>(recruitPostRepository, Mockito.never())
            .save<RecruitPost>(ArgumentMatchers.any<RecruitPost>())
    }

    @Test
    fun 모집글_수정_성공() {
        val requestDto = Requests(
            1L,
            "수정된 제목",
            "수정된 설명",
            LocalDateTime.now().plusDays(10),
            "COMPLETED"
        )

        val loginUser = Mockito.mock<User>(User::class.java)
        Mockito.`when`<Long?>(loginUser.id).thenReturn(1L)

        val profileUser = Mockito.mock<User>(User::class.java)
        Mockito.`when`<Long?>(profileUser.id).thenReturn(1L)

        val profile = Mockito.mock<Profile>(Profile::class.java)
        Mockito.`when`<User?>(profile.user).thenReturn(profileUser)
        Mockito.`when`<Long?>(profile.id).thenReturn(1L)
        Mockito.`when`<String?>(profile.name).thenReturn("테스트 프로필")
        mockImageAndType(profile)

        val post = Mockito.mock<RecruitPost>(RecruitPost::class.java)
        Mockito.`when`<Long?>(post.id).thenReturn(1L)
        Mockito.`when`<String?>(post.title).thenReturn("수정된 제목")
        Mockito.`when`<String?>(post.description).thenReturn("수정된 설명")
        Mockito.`when`<RecruitPostStatus?>(post.status).thenReturn(RecruitPostStatus.COMPLETED)
        Mockito.`when`<LocalDateTime?>(post.deadline).thenReturn(LocalDateTime.now().plusDays(10))
        Mockito.`when`<LocalDateTime?>(post.createdAt).thenReturn(LocalDateTime.now())
        Mockito.`when`<LocalDateTime?>(post.deletedAt).thenReturn(null)
        Mockito.`when`<Profile?>(post.profile).thenReturn(profile)

        Mockito.`when`<String?>(userInfo!!.username).thenReturn("providerId")
        Mockito.`when`<User?>(userService!!.findByProviderId("providerId")).thenReturn(loginUser)
        Mockito.`when`<Optional<RecruitPost>?>(recruitPostRepository!!.findById(1L))
            .thenReturn(Optional.of<RecruitPost>(post))

        val responses = recruitPostService!!.updateRecruitPost(1L, requestDto, userInfo)

        Assertions.assertNotNull(responses)
    }

    @Test
    fun 수정_삭제된_모집글이면_InvalidException() {
        val requestDto = createRequestDto()

        val post = Mockito.mock<RecruitPost>(RecruitPost::class.java)
        Mockito.`when`<LocalDateTime?>(post.deletedAt).thenReturn(LocalDateTime.now())

        Mockito.`when`<Optional<RecruitPost>?>(recruitPostRepository!!.findById(1L))
            .thenReturn(Optional.of<RecruitPost>(post))

        Assertions.assertThrows<InvalidException?>(InvalidException::class.java, Executable {
            recruitPostService!!.updateRecruitPost(1L, requestDto, userInfo!!)
        })
    }

    @Test
    fun 수정_권한없으면_ForbiddenException() {
        val requestDto = createRequestDto()

        val post = Mockito.mock<RecruitPost>(RecruitPost::class.java)
        val profile = Mockito.mock<Profile>(Profile::class.java)
        val owner = Mockito.mock<User>(User::class.java)
        val otherUser = Mockito.mock<User>(User::class.java)
        Mockito.`when`<Long?>(owner.id).thenReturn(1L)
        Mockito.`when`<Long?>(otherUser.id).thenReturn(2L)
        Mockito.`when`<User?>(profile.user).thenReturn(owner)
        mockImageAndType(profile)
        Mockito.`when`<Profile?>(post.profile).thenReturn(profile)
        Mockito.`when`<LocalDateTime?>(post.deletedAt).thenReturn(null)

        Mockito.`when`<Optional<RecruitPost>?>(recruitPostRepository!!.findById(1L))
            .thenReturn(Optional.of<RecruitPost>(post))
        Mockito.`when`<String?>(userInfo!!.username).thenReturn("providerId")
        Mockito.`when`<User?>(userService!!.findByProviderId("providerId")).thenReturn(otherUser)

        Assertions.assertThrows<ForbiddenException?>(ForbiddenException::class.java, Executable {
            recruitPostService!!.updateRecruitPost(1L, requestDto, userInfo)
        })
    }

    @Test
    fun 모집글_삭제_성공() {
        val post = Mockito.mock<RecruitPost>(RecruitPost::class.java)
        val profile = Mockito.mock<Profile>(Profile::class.java)
        val user = Mockito.mock<User>(User::class.java)
        Mockito.`when`<Long?>(user.id).thenReturn(1L)
        Mockito.`when`<User?>(profile.user).thenReturn(user)
        Mockito.`when`<Profile?>(post.profile).thenReturn(profile)

        Mockito.`when`<String?>(userInfo!!.username).thenReturn("providerId")
        Mockito.`when`<User?>(userService!!.findByProviderId("providerId")).thenReturn(user)
        Mockito.`when`<Optional<RecruitPost>?>(recruitPostRepository!!.findById(1L))
            .thenReturn(Optional.of<RecruitPost>(post))

        recruitPostService!!.deleteRecruitPost(1L, userInfo)

        Mockito.verify<RecruitPostRepository?>(recruitPostRepository).delete(post)
    }

    @Test
    fun 삭제_권한없으면_ForbiddenException() {
        val post = Mockito.mock<RecruitPost>(RecruitPost::class.java)
        val profile = Mockito.mock<Profile>(Profile::class.java)
        val postOwner = Mockito.mock<User>(User::class.java)
        val otherUser = Mockito.mock<User>(User::class.java)
        Mockito.`when`<Long?>(postOwner.id).thenReturn(1L)
        Mockito.`when`<Long?>(otherUser.id).thenReturn(2L)
        Mockito.`when`<User?>(profile.user).thenReturn(postOwner)
        Mockito.`when`<Profile?>(post.profile).thenReturn(profile)

        Mockito.`when`<String?>(userInfo!!.username).thenReturn("providerId")
        Mockito.`when`<User?>(userService!!.findByProviderId("providerId")).thenReturn(otherUser)
        Mockito.`when`<Optional<RecruitPost>?>(recruitPostRepository!!.findById(1L))
            .thenReturn(Optional.of<RecruitPost>(post))

        Assertions.assertThrows<ForbiddenException?>(ForbiddenException::class.java, Executable {
            recruitPostService!!.deleteRecruitPost(1L, userInfo)
        })

        Mockito.verify<RecruitPostRepository?>(recruitPostRepository, Mockito.never())
            .delete(ArgumentMatchers.any<RecruitPost?>())
    }

    @Test
    fun 단건조회_성공() {
        val post = Mockito.mock<RecruitPost>(RecruitPost::class.java)
        val profile = Mockito.mock<Profile>(Profile::class.java)
        mockImageAndType(profile)

        // ── 추가로 필요한 stub ──────────────────────────────
        Mockito.`when`<Long?>(post.id).thenReturn(1L)
        Mockito.`when`<String?>(post.title).thenReturn("제목")
        Mockito.`when`<String?>(post.description).thenReturn("설명")
        Mockito.`when`<RecruitPostStatus?>(post.status).thenReturn(RecruitPostStatus.RECRUITING)
        Mockito.`when`<LocalDateTime?>(post.deadline).thenReturn(LocalDateTime.now().plusDays(5))
        Mockito.`when`<LocalDateTime?>(post.createdAt).thenReturn(LocalDateTime.now())
        Mockito.`when`<LocalDateTime?>(post.deletedAt).thenReturn(null)
        Mockito.`when`<Profile?>(post.profile).thenReturn(profile)

        // ─────────────────────────────────────────────────
        Mockito.`when`<Optional<RecruitPost>?>(recruitPostRepository!!.findById(1L))
            .thenReturn(Optional.of<RecruitPost>(post))

        val result = recruitPostService!!.getRecruitPostById(1L)

        Assertions.assertNotNull(result)
    }

    @Test
    fun 단건조회_실패_NotFound() {
        Mockito.`when`<Optional<RecruitPost>?>(recruitPostRepository!!.findById(1L))
            .thenReturn(Optional.empty<RecruitPost>())

        Assertions.assertThrows<NotFoundException?>(NotFoundException::class.java, Executable {
            recruitPostService!!.getRecruitPostById(1L)
        })
    }
}