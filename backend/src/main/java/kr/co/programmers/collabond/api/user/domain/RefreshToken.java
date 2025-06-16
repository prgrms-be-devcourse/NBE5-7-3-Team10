package kr.co.programmers.collabond.api.user.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import kr.co.programmers.collabond.shared.domain.OnlyUpdatedEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken extends OnlyUpdatedEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @Size(max = 500)
    @Column(unique = true)
    private String token;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TokenStatus status = TokenStatus.VALID;

    @Builder
    private RefreshToken(User user, String token) {
        this.user = user;
        this.token = token;
    }

    public void updateStatus(TokenStatus status) {
        this.status = status;
    }
}
