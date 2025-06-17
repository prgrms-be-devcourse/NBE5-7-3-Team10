package kr.co.programmers.collabond.api.image.infrastructure


import kr.co.programmers.collabond.api.file.domain.File
import kr.co.programmers.collabond.api.image.domain.Image
import kr.co.programmers.collabond.api.profile.domain.Profile
import org.springframework.stereotype.Component

@Component
object ImageMapper {
    fun toEntity(
        profile: Profile,
        file: File,
        type: String,
        priority: Int? = null
    ): Image =
        Image(
            profile = profile,
            file = file,
            type = type,
            priority = priority
        )
}