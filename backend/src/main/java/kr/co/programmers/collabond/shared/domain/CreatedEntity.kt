package kr.co.programmers.collabond.shared.domain

import jakarta.persistence.*
import jakarta.validation.constraints.NotNull
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
abstract class CreatedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L

    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    @NotNull
    var createdAt: LocalDateTime = LocalDateTime.now()

    @Column(name = "deleted_at")
    var deletedAt: LocalDateTime? = null

    val isDeleted: Boolean
        get() = deletedAt != null
}