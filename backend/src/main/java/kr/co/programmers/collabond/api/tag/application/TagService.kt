package kr.co.programmers.collabond.api.tag.application

import kr.co.programmers.collabond.api.profile.domain.Profile
import kr.co.programmers.collabond.api.profiletag.domain.ProfileTag
import kr.co.programmers.collabond.api.profiletag.interfaces.ProfileTagMapper
import kr.co.programmers.collabond.api.tag.domain.Tag
import kr.co.programmers.collabond.api.tag.domain.TagType
import kr.co.programmers.collabond.api.tag.domain.dto.Requests
import kr.co.programmers.collabond.api.tag.domain.dto.Responses
import kr.co.programmers.collabond.api.tag.infrastructure.TagRepository
import kr.co.programmers.collabond.api.tag.interfaces.TagMapper
import kr.co.programmers.collabond.shared.exception.ErrorCode
import kr.co.programmers.collabond.shared.exception.custom.DuplicatedException
import kr.co.programmers.collabond.shared.exception.custom.InvalidException
import kr.co.programmers.collabond.shared.exception.custom.NotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TagService(
    private val tagRepository: TagRepository
) {


    @Transactional
    fun create(dto: Requests): Responses {
        if (tagRepository.existsByNameAndType(dto.name, TagType.valueOf(dto.type))) {
            throw DuplicatedException(ErrorCode.DUPLICATED_TAG)
        }
        val saved = tagRepository.save(TagMapper.toEntity(dto))
        return TagMapper.toDto(saved)
    }

    @Transactional
    fun delete(tagId: Long) {
        val tag = tagRepository.findById(tagId)
            .orElseThrow { NotFoundException(ErrorCode.TAG_NOT_FOUND) }
        tagRepository.delete(tag)
    }


    @Transactional(readOnly = true)
    fun findAll(): List<Responses> =
        tagRepository.findAll()
            .map { TagMapper.toDto(it) }


    @Transactional
    fun validateAndBindTags(profile: Profile, tagIds: List<Long>) {
        if (tagIds.size > 5) {
            throw InvalidException(ErrorCode.OVER_MAX_TAG)
        }
        val tags = tagRepository.findAllById(tagIds)
        if (tags.size != tagIds.size) {
            throw NotFoundException(ErrorCode.INCLUDE_TAG_NOT_FOUND)
        }
        val profileType = TagType.valueOf(profile.type.name)
        tags.forEach { tag ->
            if (tag.type != profileType) {
                throw InvalidException(ErrorCode.NOT_MATCH_TYPE_OF_TAG)
            }
        }
        tags.forEach { tag ->
            val profileTag: ProfileTag = ProfileTagMapper.toEntity(tag)
            profile.addTag(profileTag)
        }
    }


    @Transactional
    fun clearTags(profile: Profile) {
        profile.tags.clear()
    }
}
