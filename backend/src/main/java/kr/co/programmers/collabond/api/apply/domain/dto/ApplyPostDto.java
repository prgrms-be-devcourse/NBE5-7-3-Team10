package kr.co.programmers.collabond.api.apply.domain.dto;

import kr.co.programmers.collabond.api.apply.domain.ApplyPost;
import kr.co.programmers.collabond.api.apply.interfaces.ApplyPostMapper;
import kr.co.programmers.collabond.api.attachment.domain.dto.AttachmentDto;
import kr.co.programmers.collabond.api.profile.domain.dto.ProfileDto;
import kr.co.programmers.collabond.api.recruit.domain.dto.RecruitPostDto;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
public class ApplyPostDto {

    private Long id;

    private RecruitPostDto recruitPost;

    private ProfileDto profile;

    private String content;

    private String status;

    private List<AttachmentDto> attachmentFiles;

    private LocalDateTime createdAt;

    @Builder
    private ApplyPostDto(Long id,
                         RecruitPostDto recruitPost,
                         ProfileDto profile,
                         String content,
                         String status,
                         List<AttachmentDto> attachmentFiles,
                         LocalDateTime createdAt) {

        this.id = id;
        this.recruitPost = recruitPost;
        this.profile = profile;
        this.content = content;
        this.status = status;
        this.attachmentFiles = attachmentFiles;
        this.createdAt = createdAt;
    }

    public ApplyPostDto(
            ApplyPost applyPost
    ) {
        ApplyPostDto a = ApplyPostMapper.toDto(applyPost);
        this.id = a.getId();
        this.recruitPost = a.getRecruitPost();
        this.profile = a.getProfile();
        this.content = a.getContent();
        this.status = a.getStatus();
        this.attachmentFiles = a.getAttachmentFiles();
        this.createdAt = a.getCreatedAt();
    }
}
