package kr.co.programmers.collabond.api.user.domain

import jakarta.persistence.*
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import kr.co.programmers.collabond.shared.domain.OnlyUpdatedEntity

@Entity
class RefreshToken(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User,

    @Column(unique = true)
    @field:Size(max = 500)
    @field:NotNull
    var token: String
) : OnlyUpdatedEntity() {
    @field:NotNull
    @Enumerated(EnumType.STRING)
    var status: TokenStatus = TokenStatus.VALID

    fun updateStatus(newStatus: TokenStatus) {
        status = newStatus
    }
}
