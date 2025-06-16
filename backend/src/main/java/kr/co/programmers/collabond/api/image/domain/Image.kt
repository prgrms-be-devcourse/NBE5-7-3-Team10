package kr.co.programmers.collabond.api.image.domain

import jakarta.persistence.*
import kr.co.programmers.collabond.api.file.domain.File
import kr.co.programmers.collabond.api.profile.domain.Profile
import lombok.AccessLevel
import lombok.Builder
import lombok.Getter
import lombok.NoArgsConstructor

@Entity
@Table(name = "images")
class Image(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    var profile: Profile,

    @OneToOne(fetch = FetchType.LAZY, cascade = [CascadeType.REMOVE])
    @JoinColumn(name = "file_id", nullable = false)
    val file: File,

    @Column(nullable = false)
    val type: String,

    val priority: Int? = null
) {
    fun updateProfile(profile: Profile) {
        this.profile = profile
    }
}