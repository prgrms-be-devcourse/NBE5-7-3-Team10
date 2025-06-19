package kr.co.programmers.collabond.api.profile.application

import io.mockk.every
import io.mockk.mockk
import kr.co.programmers.collabond.api.file.application.FileService
import kr.co.programmers.collabond.api.file.domain.File
import kr.co.programmers.collabond.api.image.application.ImageService
import kr.co.programmers.collabond.api.image.domain.Image
import kr.co.programmers.collabond.api.profile.domain.Profile
import kr.co.programmers.collabond.api.profile.domain.ProfileType
import kr.co.programmers.collabond.api.profile.domain.dto.ProfileRequestDto
import kr.co.programmers.collabond.api.profile.infrastructure.ProfileRepository
import kr.co.programmers.collabond.api.tag.application.TagService
import kr.co.programmers.collabond.api.user.application.UserService
import kr.co.programmers.collabond.api.user.domain.Role
import kr.co.programmers.collabond.api.user.domain.User
import kr.co.programmers.collabond.core.auth.oauth2.OAuth2UserInfo
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.function.Executable
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.jpa.domain.Specification
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
class ProfileServiceTest {
    @Mock
    lateinit var fileService: FileService
    @Mock
    lateinit var imageService: ImageService
    @Mock
    lateinit var userService: UserService
    @Mock
    lateinit var tagService: TagService
    @Mock
    lateinit var profileRepository: ProfileRepository
    @InjectMocks
    lateinit var profileService: ProfileService

    @Test
    @DisplayName("프로필 생성 시 파일 저장 호출 및 태그 바인딩 검증")
    fun create_success() {
        // given
        val dto: ProfileRequestDto = mock(ProfileRequestDto::class.java).apply {
            `when`(type).thenReturn("IP")
            `when`(name).thenReturn("A")
            `when`(description).thenReturn("B")
            `when`(tagIds).thenReturn(listOf(1L))
            `when`(addressCode).thenReturn("TmpAddresscode")
            `when`(address).thenReturn("TmpAddress")
        }
        val profileImage: MultipartFile = mock(MultipartFile::class.java)
        val thumbnailImage: MultipartFile = mock(MultipartFile::class.java)
        val extraImages: List<MultipartFile> = emptyList()

        val oauthInfo: OAuth2UserInfo = mock(OAuth2UserInfo::class.java).apply {
            `when`(username).thenReturn("prov")
        }

        val user: User = mock(User::class.java).apply {
            `when`(role).thenReturn(Role.ROLE_IP)
            `when`(id).thenReturn(1L)
        }
        `when`(userService.findByProviderId("prov")).thenReturn(user)
        `when`(profileRepository.countByUserId(1L)).thenReturn(0L)

        val mockFile: File = mock(File::class.java)
        `when`(fileService.saveFile(profileImage)).thenReturn(mockFile)
        `when`(fileService.saveFile(thumbnailImage)).thenReturn(mockFile)
        `when`(mockFile.savedName).thenReturn("dummy.png")

        val savedProfile: Profile = mock(Profile::class.java)
        `when`(savedProfile.user).thenReturn(user)
        `when`(savedProfile.id).thenReturn(123L)
        `when`(savedProfile.type).thenReturn(ProfileType.IP)
        `when`(savedProfile.name).thenReturn("A")
        `when`(savedProfile.description).thenReturn("B")
        val now = LocalDateTime.now()
        `when`(savedProfile.createdAt).thenReturn(now)
        `when`(savedProfile.updatedAt).thenReturn(now)
        `when`(savedProfile.address).thenReturn("TmpAddress")
        `when`(savedProfile.addressCode).thenReturn("TmpAddresscode")
        `when`(savedProfile.status).thenReturn(true)
        `when`(savedProfile.tags).thenReturn(mutableListOf())

        `when`(profileRepository.save(any(Profile::class.java))).thenReturn(savedProfile)

        val profileImg: Image = mock(Image::class.java).apply {
            `when`(type).thenReturn("PROFILE")
            `when`(file).thenReturn(mockFile)
        }
        val thumbnailImg: Image = mock(Image::class.java).apply {
            `when`(type).thenReturn("THUMBNAIL")
            `when`(file).thenReturn(mockFile)
        }
        `when`(savedProfile.images).thenReturn(mutableListOf(profileImg, thumbnailImg))

        // when
        val resp = profileService.create(dto, profileImage, thumbnailImage, extraImages, oauthInfo)

        // then
        assertAll(
            "ProfileResponseDto",
            Executable { assertEquals(123L, resp.id) },
            Executable { assertEquals("IP", resp.type) },
            Executable { assertEquals("A", resp.name) },
            Executable { assertEquals("B", resp.description) },
            Executable { assertEquals("dummy.png", resp.profileImageUrl) },
            Executable { assertEquals("dummy.png", resp.thumbnailImageUrl) },
            Executable { assertTrue(resp.extraImageUrls.isEmpty()) }
        )

        verify(profileRepository).save(any(Profile::class.java))
    }

    @Test
    @DisplayName("ID로 프로필 조회 시 존재하는 ID는 Optional에 ProfileResponseDto가 담겨 있고 없으면 비어있음")
    fun findById_presentAndEmpty() {
        // given
        val existingId = 1L
        val missingId = 2L

        val profile: Profile = mock(Profile::class.java).apply {
            `when`(id).thenReturn(existingId)
            `when`(type).thenReturn(ProfileType.IP)
            `when`(name).thenReturn("TestName")
            `when`(description).thenReturn("TestDesc")
            `when`(address).thenReturn("TestAddr")
            `when`(addressCode).thenReturn("Code123")
            `when`(collaboCount).thenReturn(0)
            `when`(tags).thenReturn(mutableListOf())
            `when`(createdAt).thenReturn(LocalDateTime.now())
            `when`(updatedAt).thenReturn(LocalDateTime.now())
        }
        val user: User = mock(User::class.java).apply {
            `when`(id).thenReturn(10L)
        }
        `when`(profile.user).thenReturn(user)
        `when`(profileRepository.findById(existingId)).thenReturn(java.util.Optional.of(profile))
        `when`(profileRepository.findById(missingId)).thenReturn(java.util.Optional.empty())

        val mockFile: File = mock(File::class.java)
        `when`(mockFile.savedName).thenReturn("file_saved.png")
        val profileImg: Image = mock(Image::class.java).apply {
            `when`(type).thenReturn("PROFILE")
            `when`(file).thenReturn(mockFile)
        }
        val thumbnailImg: Image = mock(Image::class.java).apply {
            `when`(type).thenReturn("THUMBNAIL")
            `when`(file).thenReturn(mockFile)
        }
        `when`(profile.images).thenReturn(mutableListOf(profileImg, thumbnailImg))

        // when & then
        val opt = profileService.findById(existingId)
        assertTrue(opt.isPresent)
        val opt2 = profileService.findById(missingId)
        assertTrue(opt2.isEmpty)
    }

    @Test
    @DisplayName("프로필 검색")
    fun searchProfiles_returnsPage() {
        // given
        val pageable = PageRequest.of(0, 1)
        val profile: Profile = mock(Profile::class.java).apply {
            `when`(id).thenReturn(1L)
            `when`(type).thenReturn(ProfileType.IP)
            `when`(name).thenReturn("A")
            `when`(description).thenReturn("B")
            `when`(addressCode).thenReturn("110")
            `when`(address).thenReturn("Addr")
            `when`(collaboCount).thenReturn(0)
            `when`(status).thenReturn(true)
            `when`(tags).thenReturn(mutableListOf())
            `when`(createdAt).thenReturn(LocalDateTime.now())
            `when`(updatedAt).thenReturn(LocalDateTime.now())
        }
        val user: User = mock(User::class.java).apply {
            `when`(id).thenReturn(5L)
            `when`(nickname).thenReturn("nick")
        }
        `when`(profile.user).thenReturn(user)

        val file: File = mock(File::class.java).apply {
            `when`(savedName).thenReturn("dummy.png")
        }

        val img: Image = mock(Image::class.java)
        `when`(img.type).thenReturn("PROFILE")
        `when`(img.file).thenReturn(file)

        `when`(profile.images).thenReturn(mutableListOf(img))

        val mockPage = PageImpl(listOf(profile))
        `when`(profileRepository.findAll(any<Specification<Profile>>(), eq(pageable))).thenReturn(mockPage)

        // when
        val resultPage = profileService.searchProfiles(
            ProfileType.IP,
            listOf("110"),
            listOf(1L),
            pageable
        )

        // then
        assertEquals(1, resultPage.totalElements)
        assertEquals(1, resultPage.content.size)
        val dto = resultPage.content.first()
        assertEquals(1L, dto.id)
        assertEquals(5L, dto.userId)
        assertEquals("IP", dto.type)
        assertEquals("A", dto.name)
        assertEquals("dummy.png", dto.profileImageUrl)
        assertEquals("110", dto.addressCode)
        assertEquals("Addr", dto.address)
        assertEquals(0, dto.collaboCount)
        assertTrue(dto.status)
        assertEquals("nick", dto.nickname)

        verify(profileRepository).findAll(any<Specification<Profile>>(), eq(pageable))
    }
}
