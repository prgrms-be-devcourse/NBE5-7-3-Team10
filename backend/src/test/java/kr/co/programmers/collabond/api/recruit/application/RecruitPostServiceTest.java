package kr.co.programmers.collabond.api.recruit.application;

import kr.co.programmers.collabond.api.file.domain.File;
import kr.co.programmers.collabond.api.image.domain.Image;
import kr.co.programmers.collabond.api.profile.application.ProfileService;
import kr.co.programmers.collabond.api.profile.domain.Profile;
import kr.co.programmers.collabond.api.profile.domain.ProfileType;
import kr.co.programmers.collabond.api.recruit.domain.RecruitPost;
import kr.co.programmers.collabond.api.recruit.domain.RecruitPostStatus;
import kr.co.programmers.collabond.api.recruit.domain.dto.RecruitPostRequestDto;
import kr.co.programmers.collabond.api.recruit.domain.dto.RecruitPostResponseDto;
import kr.co.programmers.collabond.api.recruit.infrastructure.RecruitPostRepository;
import kr.co.programmers.collabond.api.user.application.UserService;
import kr.co.programmers.collabond.api.user.domain.User;
import kr.co.programmers.collabond.core.auth.oauth2.OAuth2UserInfo;
import kr.co.programmers.collabond.shared.exception.custom.ForbiddenException;
import kr.co.programmers.collabond.shared.exception.custom.InvalidException;
import kr.co.programmers.collabond.shared.exception.custom.NotFoundException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

class RecruitPostServiceTest {

    @Mock private ProfileService profileService;
    @Mock private UserService userService;
    @Mock private RecruitPostRepository recruitPostRepository;
    @InjectMocks private RecruitPostService recruitPostService;
    @Mock private OAuth2UserInfo userInfo;

    @BeforeEach
    void setUp() {
        openMocks(this);
    }

    private RecruitPostRequestDto createRequestDto() {
        RecruitPostRequestDto dto = new RecruitPostRequestDto();
        dto.setProfileId(1L);
        dto.setTitle("제목");
        dto.setDescription("설명");
        dto.setStatus("RECRUITING");
        dto.setDeadline(LocalDateTime.now().plusDays(5));
        return dto;
    }

    private void mockImageAndType(Profile profile) {
        File file = mock(File.class);
        when(file.getSavedName()).thenReturn("test.jpg");

        Image image = mock(Image.class);
        when(image.getType()).thenReturn("PROFILE");
        when(image.getFile()).thenReturn(file);

        when(profile.getImages()).thenReturn(List.of(image));
        when(profile.getType()).thenReturn(ProfileType.STORE);
    }

    @Test
    void 모집글_성공적으로_생성한다() {
        RecruitPostRequestDto requestDto = createRequestDto();

        User loginUser = mock(User.class);
        when(loginUser.getId()).thenReturn(1L);

        User profileUser = mock(User.class);
        when(profileUser.getId()).thenReturn(1L);

        Profile profile = mock(Profile.class);
        when(profile.getUser()).thenReturn(profileUser);
        mockImageAndType(profile);

        RecruitPost post = mock(RecruitPost.class);
        when(post.getProfile()).thenReturn(profile);

        when(userInfo.getUsername()).thenReturn("providerId");
        when(userService.findByProviderId("providerId")).thenReturn(loginUser);
        when(profileService.findByProfileId(1L)).thenReturn(profile);
        when(recruitPostRepository.save(any())).thenReturn(post);

        RecruitPostResponseDto response = recruitPostService.createRecruitPost(requestDto, userInfo);

        assertNotNull(response);
        verify(recruitPostRepository).save(any());
    }

    @Test
    void 프로필_작성자가_아닌_경우_ForbiddenException() {
        RecruitPostRequestDto requestDto = createRequestDto();

        User loginUser = mock(User.class);
        when(loginUser.getId()).thenReturn(1L);

        User otherUser = mock(User.class);
        when(otherUser.getId()).thenReturn(2L);

        Profile profile = mock(Profile.class);
        when(profile.getUser()).thenReturn(otherUser);
        mockImageAndType(profile);

        when(userInfo.getUsername()).thenReturn("providerId");
        when(userService.findByProviderId("providerId")).thenReturn(loginUser);
        when(profileService.findByProfileId(1L)).thenReturn(profile);

        assertThrows(ForbiddenException.class, () -> {
            recruitPostService.createRecruitPost(requestDto, userInfo);
        });

        verify(recruitPostRepository, never()).save(any());
    }

    @Test
    void 모집글_수정_성공() {
        RecruitPostRequestDto requestDto = createRequestDto();
        requestDto.setStatus("COMPLETED");

        User loginUser = mock(User.class);
        when(loginUser.getId()).thenReturn(1L);

        User profileUser = mock(User.class);
        when(profileUser.getId()).thenReturn(1L);

        Profile profile = mock(Profile.class);
        when(profile.getUser()).thenReturn(profileUser);
        mockImageAndType(profile);

        RecruitPost post = mock(RecruitPost.class);
        when(post.getProfile()).thenReturn(profile);
        when(post.getDeletedAt()).thenReturn(null);

        when(userInfo.getUsername()).thenReturn("providerId");
        when(userService.findByProviderId("providerId")).thenReturn(loginUser);
        when(recruitPostRepository.findById(1L)).thenReturn(Optional.of(post));

        RecruitPostResponseDto response = recruitPostService.updateRecruitPost(1L, requestDto, userInfo);

        assertNotNull(response);
    }

    @Test
    void 수정_삭제된_모집글이면_InvalidException() {
        RecruitPostRequestDto requestDto = createRequestDto();

        RecruitPost post = mock(RecruitPost.class);
        when(post.getDeletedAt()).thenReturn(LocalDateTime.now());

        when(recruitPostRepository.findById(1L)).thenReturn(Optional.of(post));

        assertThrows(InvalidException.class, () -> {
            recruitPostService.updateRecruitPost(1L, requestDto, userInfo);
        });
    }

    @Test
    void 수정_권한없으면_ForbiddenException() {
        RecruitPostRequestDto requestDto = createRequestDto();

        RecruitPost post = mock(RecruitPost.class);
        Profile profile = mock(Profile.class);
        User owner = mock(User.class);
        User otherUser = mock(User.class);
        when(owner.getId()).thenReturn(1L);
        when(otherUser.getId()).thenReturn(2L);
        when(profile.getUser()).thenReturn(owner);
        mockImageAndType(profile);
        when(post.getProfile()).thenReturn(profile);
        when(post.getDeletedAt()).thenReturn(null);

        when(recruitPostRepository.findById(1L)).thenReturn(Optional.of(post));
        when(userInfo.getUsername()).thenReturn("providerId");
        when(userService.findByProviderId("providerId")).thenReturn(otherUser);

        assertThrows(ForbiddenException.class, () -> {
            recruitPostService.updateRecruitPost(1L, requestDto, userInfo);
        });
    }

    @Test
    void 모집글_삭제_성공() {
        RecruitPost post = mock(RecruitPost.class);
        Profile profile = mock(Profile.class);
        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);
        when(profile.getUser()).thenReturn(user);
        when(post.getProfile()).thenReturn(profile);

        when(userInfo.getUsername()).thenReturn("providerId");
        when(userService.findByProviderId("providerId")).thenReturn(user);
        when(recruitPostRepository.findById(1L)).thenReturn(Optional.of(post));

        recruitPostService.deleteRecruitPost(1L, userInfo);

        verify(recruitPostRepository).delete(post);
    }

    @Test
    void 삭제_권한없으면_ForbiddenException() {
        RecruitPost post = mock(RecruitPost.class);
        Profile profile = mock(Profile.class);
        User postOwner = mock(User.class);
        User otherUser = mock(User.class);
        when(postOwner.getId()).thenReturn(1L);
        when(otherUser.getId()).thenReturn(2L);
        when(profile.getUser()).thenReturn(postOwner);
        when(post.getProfile()).thenReturn(profile);

        when(userInfo.getUsername()).thenReturn("providerId");
        when(userService.findByProviderId("providerId")).thenReturn(otherUser);
        when(recruitPostRepository.findById(1L)).thenReturn(Optional.of(post));

        assertThrows(ForbiddenException.class, () -> {
            recruitPostService.deleteRecruitPost(1L, userInfo);
        });

        verify(recruitPostRepository, never()).delete(any());
    }

    @Test
    void 단건조회_성공() {
        RecruitPost post = mock(RecruitPost.class);
        Profile profile = mock(Profile.class);
        mockImageAndType(profile);

        when(post.getProfile()).thenReturn(profile);
        when(recruitPostRepository.findById(1L)).thenReturn(Optional.of(post));

        RecruitPostResponseDto result = recruitPostService.getRecruitPostById(1L);

        assertNotNull(result);
    }

    @Test
    void 단건조회_실패_NotFound() {
        when(recruitPostRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> {
            recruitPostService.getRecruitPostById(1L);
        });
    }
}
