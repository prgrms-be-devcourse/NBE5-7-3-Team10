package kr.co.programmers.collabond.api.image.application

import kr.co.programmers.collabond.api.image.domain.Image
import kr.co.programmers.collabond.api.image.infrastructure.ImageRepository
import lombok.RequiredArgsConstructor
import org.springframework.stereotype.Service

@Service
class ImageService(
    private val imageRepository: ImageRepository
) {
    fun findByProfileIdAndType(profileId: Long, type: String): List<Image> =
        imageRepository.findByProfileIdAndType(profileId, type)

    fun findByProfileId(profileId: Long): List<Image> =
        imageRepository.findByProfileId(profileId)
}
