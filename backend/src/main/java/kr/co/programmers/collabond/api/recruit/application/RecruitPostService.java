package kr.co.programmers.collabond.api.recruit.application;

import kr.co.programmers.collabond.api.image.domain.Image;
import kr.co.programmers.collabond.api.profile.application.ProfileService;
import kr.co.programmers.collabond.api.profile.domain.Profile;
import kr.co.programmers.collabond.api.recruit.domain.RecruitPost;
import kr.co.programmers.collabond.api.recruit.domain.RecruitPostStatus;
import kr.co.programmers.collabond.api.recruit.domain.dto.RecruitPostRequestDto;
import kr.co.programmers.collabond.api.recruit.domain.dto.RecruitPostResponseDto;
import kr.co.programmers.collabond.api.recruit.infrastructure.RecruitPostRepository;
import kr.co.programmers.collabond.api.recruit.interfaces.RecruitPostMapper;
import kr.co.programmers.collabond.api.user.application.UserService;
import kr.co.programmers.collabond.api.user.domain.User;
import kr.co.programmers.collabond.core.auth.oauth2.OAuth2UserInfo;
import kr.co.programmers.collabond.shared.exception.ErrorCode;
import kr.co.programmers.collabond.shared.exception.ForbiddenException;
import kr.co.programmers.collabond.shared.exception.InvalidException;
import kr.co.programmers.collabond.shared.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RecruitPostService {

    private final ProfileService profileService;
    private final UserService userService;

    private final RecruitPostRepository recruitPostRepository;

    // 모집글 작성
    @Transactional
    public RecruitPostResponseDto createRecruitPost(RecruitPostRequestDto request,
                                                    OAuth2UserInfo userInfo) {

        Profile profile = profileService.findByProfileId(request.getProfileId());

        User loginUser = userService.findByProviderId(userInfo.getUsername());

        if (!profile.getUser().getId().equals(loginUser.getId())) {
            throw new ForbiddenException();
        }

        RecruitPost post = RecruitPostMapper.toEntity(request, profile);
        String imgPath = getProfileImgName(post);

        return RecruitPostMapper.toResponseDto(recruitPostRepository.save(post), imgPath);
    }

    // 모집글 수정
    @Transactional
    public RecruitPostResponseDto updateRecruitPost(Long recruitmentId,
                                                    RecruitPostRequestDto request,
                                                    OAuth2UserInfo userInfo) {

        RecruitPost post = recruitPostRepository.findById(recruitmentId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.RECRUIT_NOT_FOUND));

        // 소프트 삭제된 게시글은 수정할 수 없습니다.
        if (post.getDeletedAt() != null) {
            throw new InvalidException(ErrorCode.REMOVED_RECRUIT_POST);
        }

        User loginUser = userService.findByProviderId(userInfo.getUsername());

        if (!post.getProfile().getUser().getId().equals(loginUser.getId())) {
            throw new ForbiddenException();
        }

        post.update(request.getTitle(),
                request.getDescription(),
                RecruitPostStatus.valueOf(request.getStatus()),
                request.getDeadline());

        String imgPath = getProfileImgName(post);

        return RecruitPostMapper.toResponseDto(post, imgPath);
    }

    // 모집글 삭제 (소프트 삭제)
    @Transactional
    public void deleteRecruitPost(Long recruitmentId, OAuth2UserInfo userInfo) {

        RecruitPost post = recruitPostRepository.findById(recruitmentId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.RECRUIT_NOT_FOUND));

        User loginUser = userService.findByProviderId(userInfo.getUsername());

        if (!post.getProfile().getUser().getId().equals(loginUser.getId())) {
            throw new ForbiddenException();
        }

        recruitPostRepository.delete(post);
    }

    // 모집글 목록 조회
    @Transactional(readOnly = true)
    public Page<RecruitPostResponseDto> getAllRecruitPosts(RecruitPostStatus status,
                                                           String sort,
                                                           Pageable pageable) {

        Page<RecruitPost> posts = status != null
                ? recruitPostRepository.findByStatus(status, pageable)
                : recruitPostRepository.findAll(pageable);

        return posts.map(recruitPost -> {
            String imgPath = getProfileImgName(recruitPost);
            return RecruitPostMapper.toResponseDto(recruitPost, imgPath);
        });
    }

    // 회원이 작성한 모집글 조회
    @Transactional(readOnly = true)
    public Page<RecruitPostResponseDto> getRecruitPostsByUser(Long userId, Pageable pageable) {
        return recruitPostRepository.findByUserId(userId, pageable)
                .map(recruitPost -> {
                    String imgPath = getProfileImgName(recruitPost);
                    return RecruitPostMapper.toResponseDto(recruitPost, imgPath);
                });
    }

    // 프로필이 작성한 모집글 조회
    @Transactional(readOnly = true)
    public Page<RecruitPostResponseDto> getRecruitPostByProfile(Long profileId, Pageable pageable) {
        return recruitPostRepository.findByProfileId(profileId, pageable)
                .map(recruitPost -> {
                    String imgPath = getProfileImgName(recruitPost);
                    return RecruitPostMapper.toResponseDto(recruitPost, imgPath);
                });
    }

    @Transactional
    public RecruitPost findByRecruitmentId(Long recruitmentId) {
        return recruitPostRepository.findById(recruitmentId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.RECRUIT_NOT_FOUND));
    }

    private String getProfileImgName(RecruitPost recruitPost) {
        Image profileImage = recruitPost.getProfile().getImages().stream()
                .filter(i -> i.getType().equals("PROFILE"))
                .findAny()
                .orElse(null);
        return profileImage.getFile().getSavedName();
    }

    // 단건 조회 기능 - 5/23 수정
    public RecruitPostResponseDto getRecruitPostById(Long recruitmentId) {
        RecruitPost recruitPost = recruitPostRepository.findById(recruitmentId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.RECRUIT_NOT_FOUND));

        String imgPath = getProfileImgName(recruitPost);
        return RecruitPostMapper.toResponseDto(recruitPost, imgPath);
    }
}
