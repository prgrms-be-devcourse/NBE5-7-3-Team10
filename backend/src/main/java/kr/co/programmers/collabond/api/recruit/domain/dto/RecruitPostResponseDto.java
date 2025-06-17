package kr.co.programmers.collabond.api.recruit.domain.dto;

import kr.co.programmers.collabond.api.profile.domain.dto.ProfileSimpleResponseDto;
import kr.co.programmers.collabond.api.recruit.domain.RecruitPostStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class RecruitPostResponseDto {
    private Long id;
    private String title;
    private String description;
    private RecruitPostStatus status;
    private LocalDateTime deadline;
    private Long profileId;
    private String profileName;
    private ProfileSimpleResponseDto profile;
    private LocalDateTime createdAt;
    private LocalDateTime deletedAt; // 소프트 삭제 시간
}
