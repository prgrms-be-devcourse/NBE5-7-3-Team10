package kr.co.programmers.collabond.api.tag.infrastructure

import kr.co.programmers.collabond.api.tag.domain.Tag
import kr.co.programmers.collabond.api.tag.domain.TagType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository


@Repository
interface TagRepository : JpaRepository<Tag, Long> {
    fun existsByNameAndType(name: String, type: TagType): Boolean
    override fun findAll(): List<Tag> // 모든 태그 조회
}
