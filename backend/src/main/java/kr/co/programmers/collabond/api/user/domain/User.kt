package kr.co.programmers.collabond.api.user.domain

import jakarta.persistence.*
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import kr.co.programmers.collabond.api.profile.domain.Profile
import kr.co.programmers.collabond.shared.domain.UpdatedEntity
import org.hibernate.annotations.SQLDelete
import org.hibernate.annotations.SQLRestriction

@Entity
@Table(name = "users")
@SQLDelete(sql = "UPDATE users SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
class User(
    @Column(unique = true)
    @field:NotNull
    @field:Email
    var email: String,

    @field:Size(max = 20)
    @field:NotNull
    var nickname: String,

    @Enumerated(EnumType.STRING)
    @field:Size(max = 20)
    var role: Role,

    var providerId: String? = null,

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL])
    val profiles: MutableList<Profile> = mutableListOf()
) : UpdatedEntity() {

    fun update(email: String? = null, nickname: String? = null, role: Role? = null): User {
        email?.let { this.email = it }
        nickname?.let { this.nickname = it }
        role?.let { this.role = it }
        return this
    }
}