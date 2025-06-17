package kr.co.programmers.collabond.api.apply.application;

import kr.co.programmers.collabond.api.apply.domain.ApplyPost;
import kr.co.programmers.collabond.api.apply.domain.ApplyPostStatus;
import kr.co.programmers.collabond.api.apply.domain.dto.ApplyPostDto;
import kr.co.programmers.collabond.api.apply.domain.dto.ApplyPostRequestDto;
import kr.co.programmers.collabond.api.apply.domain.dto.ReceivedApplyPostsRequestDto;
import kr.co.programmers.collabond.api.apply.domain.dto.SentApplyPostsRequestDto;
import kr.co.programmers.collabond.api.apply.infrastructure.ApplyPostRepository;
import kr.co.programmers.collabond.api.apply.interfaces.ApplyPostMapper;
import kr.co.programmers.collabond.api.attachment.domain.Attachment;
import kr.co.programmers.collabond.api.attachment.interfaces.AttachmentMapper;
import kr.co.programmers.collabond.api.file.application.FileService;
import kr.co.programmers.collabond.api.file.domain.File;
import kr.co.programmers.collabond.api.mail.service.MailService;
import kr.co.programmers.collabond.api.profile.application.ProfileService;
import kr.co.programmers.collabond.api.profile.domain.Profile;
import kr.co.programmers.collabond.api.recruit.application.RecruitPostService;
import kr.co.programmers.collabond.api.recruit.domain.RecruitPost;
import kr.co.programmers.collabond.api.user.application.UserService;
import kr.co.programmers.collabond.api.user.domain.User;
import kr.co.programmers.collabond.core.auth.oauth2.OAuth2UserInfo;
import kr.co.programmers.collabond.shared.exception.ErrorCode;
import kr.co.programmers.collabond.shared.exception.custom.ForbiddenException;
import kr.co.programmers.collabond.shared.exception.custom.InvalidException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApplyPostService {

    private final RecruitPostService recruitPostService;
    private final ProfileService profileService;
    private final UserService userService;
    private final FileService fileService;
    private final MailService mailService;

    private final ApplyPostRepository applyPostRepository;

    @Transactional
    public ApplyPostDto applyPost(Long recruitmentId,
                                  ApplyPostRequestDto request,
                                  List<MultipartFile> files) {

        RecruitPost recruitPost = recruitPostService.findByRecruitmentId(recruitmentId);
        Profile profile = profileService.findByProfileId(request.getProfileId());

        if (recruitPost.getProfile().getType().equals(profile.getType())) {
            throw new InvalidException(ErrorCode.REQUEST_TO_SAME_ROLE);
        }

        ApplyPost applyPost = ApplyPostMapper.toEntity(recruitPost, profile, request);

        if (files != null && !files.isEmpty()) {
            List<File> savedFiles = fileService.saveFiles(files);
            ArrayList<Attachment> attachments = new ArrayList<>();
            for (File savedFile : savedFiles) {
                attachments.add(AttachmentMapper.toEntity(applyPost, savedFile));
                log.debug("savedFile.getOriginName() = {}", savedFile.getOriginName());
                log.debug("savedFile.getSavedName() = {}", savedFile.getSavedName());
            }
            applyPost.updateAttachment(attachments);
        }

        ApplyPost save = applyPostRepository.save(applyPost);

        mailService.sendReceivedApplyMail(recruitPost, profile, save.getCreatedAt());

        return ApplyPostMapper.toDto(save);
    }

    @Transactional(readOnly = true)
    public Page<ApplyPostDto> findSentApplyPosts(SentApplyPostsRequestDto request,
                                                 OAuth2UserInfo userInfo,
                                                 Pageable pageable) {

        User user = userService.findByProviderId(userInfo.getUsername());

        Page<ApplyPostDto> applyPosts = request.getStatus() != null
                ? applyPostRepository
                .findAllSentByUserIdAndStatus(user.getId(), request.getStatus(), pageable)
                : applyPostRepository
                .findAllSentByUser(user.getId(), pageable);
        return applyPosts;
    }

    @Transactional(readOnly = true)
    public Page<ApplyPostDto> findReceivedApplyPosts(
            ReceivedApplyPostsRequestDto request,
            OAuth2UserInfo userInfo,
            Pageable pageable) {

        User user = userService.findByProviderId(userInfo.getUsername());

        Page<ApplyPostDto> applyPosts = request.getStatus() != null
                ? applyPostRepository
                .findAllReceivedByUserIdAndStatus(user.getId(), request.getStatus(), pageable)
                : applyPostRepository
                .findAllReceivedByUser(user.getId(), pageable);

        return applyPosts;
    }

    @Transactional
    public void acceptApply(Long applicationId, OAuth2UserInfo userInfo) {

        ApplyPost receivedApply = applyPostRepository.findById(applicationId)
                .orElseThrow(() -> new InvalidException(ErrorCode.APPLY_NOT_FOUND));

        User loginUser = userService.findByProviderId(userInfo.getUsername());

        if (!receivedApply.getRecruitPost().getProfile().getUser().getId()
                .equals(loginUser.getId())) {
            throw new ForbiddenException(ErrorCode.FORBIDDEN_REQUEST);
        }

        receivedApply.updateStatus(ApplyPostStatus.ACCEPTED);
        updateCollaboCount(receivedApply);
        LocalDateTime acceptedAt = LocalDateTime.now();

        mailService.sendBondCompletionEmails(receivedApply, acceptedAt);
    }

    private void updateCollaboCount(ApplyPost applyPost) {
        applyPost.getProfile().updateCollaboCount();
        applyPost.getRecruitPost().getProfile().updateCollaboCount();
    }
}
