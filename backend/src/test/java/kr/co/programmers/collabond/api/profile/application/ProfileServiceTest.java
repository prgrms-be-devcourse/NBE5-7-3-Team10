package kr.co.programmers.collabond.api.profile.application;

import kr.co.programmers.collabond.api.file.application.FileService;
import kr.co.programmers.collabond.api.file.domain.File;
import kr.co.programmers.collabond.api.image.application.ImageService;
import kr.co.programmers.collabond.api.image.domain.Image;
import kr.co.programmers.collabond.api.profile.domain.Profile;
import kr.co.programmers.collabond.api.profile.domain.ProfileType;
import kr.co.programmers.collabond.api.profile.domain.dto.ProfileDetailResponseDto;
import kr.co.programmers.collabond.api.profile.domain.dto.ProfileRequestDto;
import kr.co.programmers.collabond.api.profile.domain.dto.ProfileResponseDto;
import kr.co.programmers.collabond.api.profile.infrastructure.ProfileRepository;
import kr.co.programmers.collabond.api.tag.application.TagService;
import kr.co.programmers.collabond.api.user.application.UserService;
import kr.co.programmers.collabond.api.user.domain.Role;
import kr.co.programmers.collabond.api.user.domain.User;
import kr.co.programmers.collabond.core.auth.oauth2.OAuth2UserInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock
    FileService fileService;

    @Mock
    ImageService imageService;

    @Mock
    UserService userService;

    @Mock
    TagService tagService;

    @Mock
    ProfileRepository profileRepository;

    @InjectMocks
    ProfileService profileService;

    @Test
    @DisplayName("프로필 생성 시 파일 저장 호출 및 태그 바인딩 검증")
    void create_success() {
        // given
        ProfileRequestDto dto = mock(ProfileRequestDto.class);
        when(dto.getType()).thenReturn("IP");
        when(dto.getName()).thenReturn("A");
        when(dto.getDescription()).thenReturn("B");
        when(dto.getTagIds()).thenReturn(List.of(1l));
        when(dto.getAddressCode()).thenReturn("TmpAddresscode");
        when(dto.getAddress()).thenReturn("TmpAddress");

        MultipartFile profileImage = mock(MultipartFile.class);
        MultipartFile thumbnailImage = mock(MultipartFile.class);
        List<MultipartFile> extraImages = Collections.emptyList();

        OAuth2UserInfo oauthInfo = mock(OAuth2UserInfo.class);
        when(oauthInfo.getUsername()).thenReturn("prov");

        User user = mock(User.class);
        when(user.getRole()).thenReturn(Role.ROLE_IP);
        when(user.getId()).thenReturn(1L);
        when(userService.findByProviderId("prov")).thenReturn(user);

        when(profileRepository.countByUserId(1L)).thenReturn(0L);

        File mockFile = mock(File.class);
        when(fileService.saveFile(profileImage)).thenReturn(mockFile);
        when(fileService.saveFile(thumbnailImage)).thenReturn(mockFile);
        when(mockFile.getSavedName()).thenReturn("dummy.png");

        Profile savedProfile = mock(Profile.class);
        when(profileRepository.save(any(Profile.class))).thenReturn(savedProfile);
        when(savedProfile.getUser()).thenReturn(user);
        when(savedProfile.getId()).thenReturn(123L);
        when(savedProfile.getType()).thenReturn(ProfileType.IP);
        when(savedProfile.getName()).thenReturn("A");
        when(savedProfile.getDescription()).thenReturn("B");
        LocalDateTime now = LocalDateTime.now();
        when(savedProfile.getCreatedAt()).thenReturn(now);
        when(savedProfile.getUpdatedAt()).thenReturn(now);
        when(savedProfile.getAddress()).thenReturn("TmpAddress");
        when(savedProfile.getAddressCode()).thenReturn("TmpAddresscode");
        when(savedProfile.getStatus()).thenReturn(true);
        when(savedProfile.getTags()).thenReturn(Collections.emptyList());

        Image profileImg = mock(Image.class);
        when(profileImg.getType()).thenReturn("PROFILE");
        when(profileImg.getFile()).thenReturn(mockFile);

        Image thumbnailImg = mock(Image.class);
        when(thumbnailImg.getType()).thenReturn("THUMBNAIL");
        when(thumbnailImg.getFile()).thenReturn(mockFile);

        when(savedProfile.getImages())
                .thenReturn(List.of(profileImg, thumbnailImg));

        // when
        ProfileResponseDto resp = profileService.create(
                dto, profileImage, thumbnailImage, extraImages, oauthInfo);

        // then
        assertAll("ProfileResponseDto",
                () -> assertEquals(123L,   resp.getId()),
                () -> assertEquals("IP",   resp.getType()),
                () -> assertEquals("A",    resp.getName()),
                () -> assertEquals("B",    resp.getDescription()),
                () -> assertEquals("dummy.png", resp.getProfileImageUrl()),
                () -> assertEquals("dummy.png", resp.getThumbnailImageUrl()),
                () -> assertTrue(resp.getExtraImageUrls().isEmpty())
        );

        verify(profileRepository).save(any(Profile.class));
    }

    @Test
    @DisplayName("ID로 프로필 조회 시 존재하는 ID는 Optional에 ProfileResponseDto가 담겨 있고 없으면 비어있음")
    void findById_presentAndEmpty() {
        // given
        long existingId = 1L;
        long missingId = 2L;

        Profile profile = mock(Profile.class);
        when(profile.getId()).thenReturn(existingId);
        when(profile.getType()).thenReturn(ProfileType.IP);
        when(profile.getName()).thenReturn("TestName");
        when(profile.getDescription()).thenReturn("TestDesc");
        when(profile.getAddress()).thenReturn("TestAddr");
        when(profile.getAddressCode()).thenReturn("Code123");
        when(profile.getCollaboCount()).thenReturn(0);
        when(profile.getTags()).thenReturn(Collections.emptyList());
        when(profile.getCreatedAt()).thenReturn(LocalDateTime.now());
        when(profile.getUpdatedAt()).thenReturn(LocalDateTime.now());

        User user = mock(User.class);
        when(user.getId()).thenReturn(10L);
        when(profile.getUser()).thenReturn(user);

        when(profileRepository.findById(existingId))
                .thenReturn(Optional.of(profile));
        when(profileRepository.findById(missingId))
                .thenReturn(Optional.empty());

        File mockFile = mock(File.class);
        when(mockFile.getSavedName()).thenReturn("file_saved.png");

        Image profileImg = mock(Image.class);
        when(profileImg.getType()).thenReturn("PROFILE");
        when(profileImg.getFile()).thenReturn(mockFile);

        Image thumbnailImg = mock(Image.class);
        when(thumbnailImg.getType()).thenReturn("THUMBNAIL");
        when(thumbnailImg.getFile()).thenReturn(mockFile);
        when(profile.getImages())
                .thenReturn(List.of(profileImg, thumbnailImg));

        Optional<ProfileResponseDto> opt = profileService.findById(existingId);
        assertTrue(opt.isPresent());
        Optional<ProfileResponseDto> opt2 = profileService.findById(missingId);
        assertTrue(opt2.isEmpty());
    }

    @Test
    @DisplayName("프로필 검색")
    void searchProfiles_returnsPage() {
        // given
        PageRequest pageable = PageRequest.of(0, 1);

        Profile profile = mock(Profile.class);
        when(profile.getId()).thenReturn(1L);
        when(profile.getType()).thenReturn(ProfileType.IP);
        when(profile.getName()).thenReturn("A");
        when(profile.getDescription()).thenReturn("B");
        when(profile.getAddressCode()).thenReturn("110");
        when(profile.getAddress()).thenReturn("Addr");
        when(profile.getCollaboCount()).thenReturn(0);
        when(profile.getStatus()).thenReturn(true);
        when(profile.getTags()).thenReturn(Collections.emptyList());
        when(profile.getCreatedAt()).thenReturn(LocalDateTime.now());
        when(profile.getUpdatedAt()).thenReturn(LocalDateTime.now());

        User user = mock(User.class);
        when(user.getId()).thenReturn(5L);
        when(user.getNickname()).thenReturn("nick");
        when(profile.getUser()).thenReturn(user);

        File file = mock(File.class);
        when(file.getSavedName()).thenReturn("dummy.png");
        Image img = mock(Image.class);
        when(img.getType()).thenReturn("PROFILE");
        when(img.getFile()).thenReturn(file);
        when(profile.getImages()).thenReturn(List.of(img));

        Page<Profile> mockPage = new PageImpl<>(List.of(profile));
        when(profileRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(mockPage);

        // when
        Page<ProfileDetailResponseDto> resultPage = profileService.searchProfiles(
                ProfileType.IP,
                List.of("110"),
                List.of(1L),
                pageable
        );

        // then
        assertEquals(1, resultPage.getTotalElements());
        assertEquals(1, resultPage.getContent().size());

        ProfileDetailResponseDto dto = resultPage.getContent().getFirst();
        assertEquals(1L, dto.getId());
        assertEquals(5L, dto.getUserId());
        assertEquals("IP", dto.getType());
        assertEquals("A",dto.getName());
        assertEquals("dummy.png", dto.getProfileImageUrl());
        assertEquals("110", dto.getAddressCode());
        assertEquals("Addr", dto.getAddress());
        assertEquals(0, dto.getCollaboCount());
        assertTrue(dto.getStatus());
        assertEquals("nick", dto.getNickname());

        verify(profileRepository).findAll(any(Specification.class), eq(pageable));
    }
}