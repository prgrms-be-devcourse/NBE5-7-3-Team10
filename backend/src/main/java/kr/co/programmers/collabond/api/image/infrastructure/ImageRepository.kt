package kr.co.programmers.collabond.api.image.infrastructure

import kr.co.programmers.collabond.api.image.domain.Image
import org.springframework.data.jpa.repository.JpaRepository

interface ImageRepository : JpaRepository<Image, Long> {
    fun findByProfileIdAndType(profileId: Long, type: String): List<Image>
    fun findByProfileId(profileId: Long): List<Image>
}