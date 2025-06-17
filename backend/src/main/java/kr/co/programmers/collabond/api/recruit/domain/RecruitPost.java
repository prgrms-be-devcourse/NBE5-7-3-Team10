package kr.co.programmers.collabond.api.recruit.domain;

import jakarta.persistence.*;
import kr.co.programmers.collabond.api.profile.domain.Profile;
import kr.co.programmers.collabond.shared.domain.UpdatedEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity
@Table(name = "recruit_posts")
@Getter
@SQLDelete(sql = "UPDATE recruit_posts SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class RecruitPost extends UpdatedEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id")
    private Profile profile;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RecruitPostStatus status;

    @Column(nullable = false)
    private LocalDateTime deadline;

    @Builder
    private RecruitPost(Profile profile, String title, String description,
                        RecruitPostStatus status, LocalDateTime deadline) {

        this.profile = profile;
        this.title = title;
        this.description = description;
        this.status = status;
        this.deadline = deadline;
    }

    public RecruitPost update(String title, String description,
                              RecruitPostStatus status, LocalDateTime deadline) {

        if (title != null) {
            this.title = title;
        }

        if (description != null) {
            this.description = description;
        }

        if (status != null) {
            this.status = status;
        }

        if (deadline != null) {
            this.deadline = deadline;
        }

        return this;
    }
}