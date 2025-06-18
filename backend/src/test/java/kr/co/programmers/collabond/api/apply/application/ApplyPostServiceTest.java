package kr.co.programmers.collabond.api.apply.application;

import kr.co.programmers.collabond.api.apply.domain.ApplyPost;
import kr.co.programmers.collabond.api.apply.domain.ApplyPostStatus;
import kr.co.programmers.collabond.api.apply.domain.dto.ApplyPostDto;
import kr.co.programmers.collabond.api.apply.domain.dto.ApplyPostRequestDto;
import kr.co.programmers.collabond.api.apply.domain.dto.ReceivedApplyPostsRequestDto;
import kr.co.programmers.collabond.api.apply.domain.dto.SentApplyPostsRequestDto;
import kr.co.programmers.collabond.api.apply.infrastructure.ApplyPostRepository;
import kr.co.programmers.collabond.api.attachment.domain.Attachment;
import kr.co.programmers.collabond.api.file.application.FileService;
import kr.co.programmers.collabond.api.file.domain.File;
import kr.co.programmers.collabond.api.mail.service.MailService;
import kr.co.programmers.collabond.api.profile.application.ProfileService;
import kr.co.programmers.collabond.api.profile.domain.Profile;
import kr.co.programmers.collabond.api.profile.domain.ProfileType;
import kr.co.programmers.collabond.api.recruit.application.RecruitPostService;
import kr.co.programmers.collabond.api.recruit.domain.RecruitPost;
import kr.co.programmers.collabond.api.recruit.domain.RecruitPostStatus;
import kr.co.programmers.collabond.api.user.application.UserService;
import kr.co.programmers.collabond.api.user.domain.User;
import kr.co.programmers.collabond.core.auth.oauth2.OAuth2UserInfo;
import kr.co.programmers.collabond.shared.exception.ErrorCode;
import kr.co.programmers.collabond.shared.exception.ForbiddenException;
import kr.co.programmers.collabond.shared.exception.InvalidException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplyPostServiceTest {

    @Mock
    private RecruitPostService recruitPostService;
    @Mock
    private ProfileService profileService;
    @Mock
    private UserService userService;
    @Mock
    private FileService fileService;
    @Mock
    private MailService mailService;
    @Mock
    private ApplyPostRepository applyPostRepository;

    @InjectMocks
    private ApplyPostService applyPostService;

    @Test
    @DisplayName("지원서 제출 테스트")
    void applyPost_success() {
        // given
        Long recruitmentId = 1L;
        Long profileId = 2L;
        Long userId = 3L;
        String recruitProfileName = "recruitProfileName";
        String recruitTitle = "recruitTitle";
        String recruitDescription = "recruitDescription";
        LocalDateTime deadline = LocalDateTime.now();
        String profileName = "profileName";
        String profileDescription = "profileDescription";


        ApplyPostRequestDto request = new ApplyPostRequestDto(profileId, "내용");
        RecruitPost recruitPost = mock(RecruitPost.class);
        Profile profile = mock(Profile.class);
        Profile recruitProfile = mock(Profile.class);
        User user = mock(User.class);
        ApplyPost applyPost = mock(ApplyPost.class);

        // Setup profile types
        when(recruitPost.getProfile()).thenReturn(recruitProfile);
        when(recruitPost.getTitle()).thenReturn(recruitTitle);
        when(recruitPost.getDescription()).thenReturn(recruitDescription);
        when(recruitPost.getDeadline()).thenReturn(deadline);
        when(recruitPost.getStatus()).thenReturn(RecruitPostStatus.RECRUITING);
        when(recruitProfile.getType()).thenReturn(ProfileType.STORE);
        when(recruitProfile.getName()).thenReturn(recruitProfileName);
        when(profile.getName()).thenReturn(profileName);
        when(profile.getDescription()).thenReturn(profileDescription);
        when(profile.getType()).thenReturn(ProfileType.IP);
        when(profile.getUser()).thenReturn(user);
        when(user.getId()).thenReturn(userId);

        when(recruitPostService.findByRecruitmentId(recruitmentId)).thenReturn(recruitPost);
        when(profileService.findByProfileId(profileId)).thenReturn(profile);

        when(applyPostRepository.save(any(ApplyPost.class))).thenAnswer(invocation -> {
            ApplyPost savedApplyPost = invocation.getArgument(0);

            when(applyPost.getId()).thenReturn(1L);
            when(applyPost.getRecruitPost()).thenReturn(recruitPost);
            when(applyPost.getProfile()).thenReturn(profile);
            when(applyPost.getContent()).thenReturn(request.getContent());
            when(applyPost.getStatus()).thenReturn(ApplyPostStatus.PENDING);
            when(applyPost.getAttachments()).thenReturn(null);
            when(applyPost.getCreatedAt()).thenReturn(LocalDateTime.now());
            return applyPost;
        });

        // when
        ApplyPostDto result = applyPostService.applyPost(recruitmentId, request, null);

        // then
        assertNotNull(result);
        verify(applyPostRepository).save(any(ApplyPost.class));
        verify(mailService).sendReceivedApplyMail(eq(recruitPost), eq(profile), any());
    }

    @Test
    @DisplayName("파일이 있는 지원서 제출 테스트")
    void applyPost_withFiles_success() {
        // given
        Long recruitmentId = 1L;
        Long profileId = 2L;
        Long userId = 3L;
        String recruitProfileName = "recruitProfileName";
        String recruitTitle = "recruitTitle";
        String recruitDescription = "recruitDescription";
        LocalDateTime deadline = LocalDateTime.now();
        String profileName = "profileName";
        String profileDescription = "profileDescription";

        ApplyPostRequestDto request = new ApplyPostRequestDto(profileId, "내용");
        RecruitPost recruitPost = mock(RecruitPost.class);
        Profile profile = mock(Profile.class);
        Profile recruitProfile = mock(Profile.class);
        User user = mock(User.class);
        ApplyPost applyPost = mock(ApplyPost.class);

        List<MultipartFile> files = new ArrayList<>();
        files.add(mock(MultipartFile.class));
        List<File> savedFiles = new ArrayList<>();
        File savedFile = mock(File.class);
        savedFiles.add(savedFile);

        List<Attachment> attachments = new ArrayList<>();
        Attachment attachment = mock(Attachment.class);
        attachments.add(attachment);

        // Setup profile types
        when(recruitPost.getProfile()).thenReturn(recruitProfile);
        when(recruitPost.getTitle()).thenReturn(recruitTitle);
        when(recruitPost.getDescription()).thenReturn(recruitDescription);
        when(recruitPost.getDeadline()).thenReturn(deadline);
        when(recruitPost.getStatus()).thenReturn(RecruitPostStatus.RECRUITING);
        when(recruitProfile.getType()).thenReturn(ProfileType.STORE);
        when(recruitProfile.getName()).thenReturn(recruitProfileName);
        when(profile.getName()).thenReturn(profileName);
        when(profile.getDescription()).thenReturn(profileDescription);
        when(profile.getType()).thenReturn(ProfileType.IP);
        when(profile.getUser()).thenReturn(user);
        when(user.getId()).thenReturn(userId);

        // Setup file mocks
        when(savedFile.getOriginName()).thenReturn("originalName.pdf");
        when(savedFile.getSavedName()).thenReturn("savedName.pdf");
        when(attachment.getFile()).thenReturn(savedFile);

        // Setup service mocks
        when(recruitPostService.findByRecruitmentId(recruitmentId)).thenReturn(recruitPost);
        when(profileService.findByProfileId(profileId)).thenReturn(profile);
        when(fileService.saveFiles(files)).thenReturn(savedFiles);

        when(applyPostRepository.save(any(ApplyPost.class))).thenAnswer(invocation -> {
            ApplyPost savedApplyPost = invocation.getArgument(0);

            when(applyPost.getId()).thenReturn(1L);
            when(applyPost.getRecruitPost()).thenReturn(recruitPost);
            when(applyPost.getProfile()).thenReturn(profile);
            when(applyPost.getContent()).thenReturn(request.getContent());
            when(applyPost.getStatus()).thenReturn(ApplyPostStatus.PENDING);
            when(applyPost.getAttachments()).thenReturn(attachments);
            when(applyPost.getCreatedAt()).thenReturn(LocalDateTime.now());
            return applyPost;
        });

        // when
        ApplyPostDto result = applyPostService.applyPost(recruitmentId, request, files);

        // then
        assertNotNull(result);
        verify(fileService).saveFiles(files);
        verify(applyPostRepository).save(any(ApplyPost.class));
        verify(mailService).sendReceivedApplyMail(eq(recruitPost), eq(profile), any());
    }

    @Test
    @DisplayName("같은 타입의 프로필로 지원서를 제출하면 InvalidException을 발생시킨다")
    void applyPost_withSameProfileType_fail() {
        // given
        Long recruitmentId = 1L;
        Long profileId = 2L;
        ApplyPostRequestDto request = new ApplyPostRequestDto(profileId, "내용");
        RecruitPost recruitPost = mock(RecruitPost.class);
        Profile recruitProfile = mock(Profile.class);
        Profile applyProfile = mock(Profile.class);

        when(recruitPost.getProfile()).thenReturn(recruitProfile);
        when(recruitProfile.getType()).thenReturn(ProfileType.STORE);
        when(applyProfile.getType()).thenReturn(ProfileType.STORE);
        when(recruitPostService.findByRecruitmentId(recruitmentId)).thenReturn(recruitPost);
        when(profileService.findByProfileId(profileId)).thenReturn(applyProfile);

        // when & then
        InvalidException exception = assertThrows(InvalidException.class, () -> {
            applyPostService.applyPost(recruitmentId, request, null);
        });
        assertEquals(ErrorCode.REQUEST_TO_SAME_ROLE.getMessage(), exception.getMessage());
    }

    @Test
    @DisplayName("status 없이 보낸 지원서를 호출하면 모든 보낸 지원서를 불러온다")
    void findSentApplyPosts_withoutStatus() {
        // given
        SentApplyPostsRequestDto request = new SentApplyPostsRequestDto();
        OAuth2UserInfo userInfo = mock(OAuth2UserInfo.class);
        User user = mock(User.class);
        Pageable pageable = mock(Pageable.class);
        Page<ApplyPostDto> expectedPage = new PageImpl<>(List.of(mock(ApplyPostDto.class)));

        when(userInfo.getUsername()).thenReturn("username");
        when(userService.findByProviderId("username")).thenReturn(user);
        when(user.getId()).thenReturn(1L);
        when(applyPostRepository.findAllSentByUser(1L, pageable)).thenReturn(expectedPage);

        // when
        Page<ApplyPostDto> result = applyPostService.findSentApplyPosts(request, userInfo, pageable);

        // then
        assertNotNull(result);
        assertEquals(expectedPage, result);
        verify(applyPostRepository).findAllSentByUser(1L, pageable);
        verify(applyPostRepository, never()).findAllSentByUserIdAndStatus(anyLong(), anyString(), any(Pageable.class));
    }

    @Test
    @DisplayName("status와 함께 보낸 지원서를 호출하면 필터링된 보낸 지원서를 불러온다")
    void findSentApplyPosts_withStatus() {
        // given
        SentApplyPostsRequestDto request = mock(SentApplyPostsRequestDto.class);
        OAuth2UserInfo userInfo = mock(OAuth2UserInfo.class);
        User user = mock(User.class);
        Pageable pageable = mock(Pageable.class);
        Page<ApplyPostDto> expectedPage = new PageImpl<>(List.of(mock(ApplyPostDto.class)));

        when(request.getStatus()).thenReturn("PENDING");
        when(userInfo.getUsername()).thenReturn("username");
        when(userService.findByProviderId("username")).thenReturn(user);
        when(user.getId()).thenReturn(1L);
        when(applyPostRepository.findAllSentByUserIdAndStatus(1L, "PENDING", pageable)).thenReturn(expectedPage);

        // when
        Page<ApplyPostDto> result = applyPostService.findSentApplyPosts(request, userInfo, pageable);

        // then
        assertNotNull(result);
        assertEquals(expectedPage, result);
        verify(applyPostRepository).findAllSentByUserIdAndStatus(1L, "PENDING", pageable);
        verify(applyPostRepository, never()).findAllSentByUser(anyLong(), any(Pageable.class));
    }

    @Test
    @DisplayName("status 없이 받은 지원서를 호출하면 모든 받은 지원서를 불러온다")
    void findReceivedApplyPosts_withoutStatus() {
        // given
        ReceivedApplyPostsRequestDto request = new ReceivedApplyPostsRequestDto();
        OAuth2UserInfo userInfo = mock(OAuth2UserInfo.class);
        User user = mock(User.class);
        Pageable pageable = mock(Pageable.class);
        Page<ApplyPostDto> expectedPage = new PageImpl<>(List.of(mock(ApplyPostDto.class)));

        when(userInfo.getUsername()).thenReturn("username");
        when(userService.findByProviderId("username")).thenReturn(user);
        when(user.getId()).thenReturn(1L);
        when(applyPostRepository.findAllReceivedByUser(1L, pageable)).thenReturn(expectedPage);

        // when
        Page<ApplyPostDto> result = applyPostService.findReceivedApplyPosts(request, userInfo, pageable);

        // then
        assertNotNull(result);
        assertEquals(expectedPage, result);
        verify(applyPostRepository).findAllReceivedByUser(1L, pageable);
        verify(applyPostRepository, never()).findAllReceivedByUserIdAndStatus(anyLong(), anyString(), any(Pageable.class));
    }

    @Test
    @DisplayName("status와 함께 받은 지원서를 호출하면 필터링된 받은 지원서를 불러온다")
    void findReceivedApplyPosts_withStatus() {
        // given
        ReceivedApplyPostsRequestDto request = mock(ReceivedApplyPostsRequestDto.class);
        OAuth2UserInfo userInfo = mock(OAuth2UserInfo.class);
        User user = mock(User.class);
        Pageable pageable = mock(Pageable.class);
        Page<ApplyPostDto> expectedPage = new PageImpl<>(List.of(mock(ApplyPostDto.class)));

        when(request.getStatus()).thenReturn("PENDING");
        when(userInfo.getUsername()).thenReturn("username");
        when(userService.findByProviderId("username")).thenReturn(user);
        when(user.getId()).thenReturn(1L);
        when(applyPostRepository.findAllReceivedByUserIdAndStatus(1L, "PENDING", pageable)).thenReturn(expectedPage);

        // when
        Page<ApplyPostDto> result = applyPostService.findReceivedApplyPosts(request, userInfo, pageable);

        // then
        assertNotNull(result);
        assertEquals(expectedPage, result);
        verify(applyPostRepository).findAllReceivedByUserIdAndStatus(1L, "PENDING", pageable);
        verify(applyPostRepository, never()).findAllReceivedByUser(anyLong(), any(Pageable.class));
    }

    @Test
    @DisplayName("지원서 수락은 상태를 업데이트하고 메일을 전송한다")
    void acceptApply_success() {
        // given
        Long applicationId = 1L;
        OAuth2UserInfo userInfo = mock(OAuth2UserInfo.class);
        User user = mock(User.class);
        ApplyPost applyPost = mock(ApplyPost.class);
        RecruitPost recruitPost = mock(RecruitPost.class);
        Profile recruitProfile = mock(Profile.class);
        Profile applyProfile = mock(Profile.class);

        when(userInfo.getUsername()).thenReturn("username");
        when(userService.findByProviderId("username")).thenReturn(user);
        when(user.getId()).thenReturn(1L);
        when(applyPostRepository.findById(applicationId)).thenReturn(Optional.of(applyPost));
        when(applyPost.getRecruitPost()).thenReturn(recruitPost);
        when(recruitPost.getProfile()).thenReturn(recruitProfile);
        when(recruitProfile.getUser()).thenReturn(user);
        when(applyPost.getProfile()).thenReturn(applyProfile);

        // when
        applyPostService.acceptApply(applicationId, userInfo);

        // then
        verify(applyPost).updateStatus(ApplyPostStatus.ACCEPTED);
        verify(applyProfile).updateCollaboCount();
        verify(recruitProfile).updateCollaboCount();
        verify(mailService).sendBondCompletionEmails(eq(applyPost), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("존재하지 않는 지원서를 수락하면 InvalidException을 발생시킨다")
    void acceptApply_withNonExistingApplication_fail() {
        // given
        Long applicationId = 1L;
        OAuth2UserInfo userInfo = mock(OAuth2UserInfo.class);

        when(applyPostRepository.findById(applicationId)).thenReturn(Optional.empty());

        // when & then
        InvalidException exception = assertThrows(InvalidException.class, () -> {
            applyPostService.acceptApply(applicationId, userInfo);
        });
        assertEquals(ErrorCode.APPLY_NOT_FOUND.getMessage(), exception.getMessage());
    }

    @Test
    @DisplayName("로그인된 프로필이 공고 작성자가 아닌상태에서 수락하려한다면 ForbiddenException을 발생시킨다")
    void acceptApply_withUnauthorizedUser_fail() {
        // given
        Long applicationId = 1L;
        OAuth2UserInfo userInfo = mock(OAuth2UserInfo.class);
        User loginUser = mock(User.class);
        User recruitUser = mock(User.class);
        ApplyPost applyPost = mock(ApplyPost.class);
        RecruitPost recruitPost = mock(RecruitPost.class);
        Profile recruitProfile = mock(Profile.class);

        when(userInfo.getUsername()).thenReturn("username");
        when(userService.findByProviderId("username")).thenReturn(loginUser);
        when(loginUser.getId()).thenReturn(1L);
        when(applyPostRepository.findById(applicationId)).thenReturn(Optional.of(applyPost));
        when(applyPost.getRecruitPost()).thenReturn(recruitPost);
        when(recruitPost.getProfile()).thenReturn(recruitProfile);
        when(recruitProfile.getUser()).thenReturn(recruitUser);
        when(recruitUser.getId()).thenReturn(2L);

        // when & then
        ForbiddenException exception = assertThrows(ForbiddenException.class, () -> {
            applyPostService.acceptApply(applicationId, userInfo);
        });
        assertEquals(ErrorCode.FORBIDDEN_REQUEST.getMessage(), exception.getMessage());
    }
}
