package kr.co.programmers.collabond.api.tag.interfaces

import kr.co.programmers.collabond.api.tag.domain.Tag
import kr.co.programmers.collabond.api.tag.domain.TagType
import kr.co.programmers.collabond.api.tag.domain.dto.Requests
import kr.co.programmers.collabond.api.tag.domain.dto.Responses

object TagMapper {
    fun toEntity(dto: Requests): Tag =
        Tag(
            name = dto.name,
            type = TagType.valueOf(dto.type)
        )

    fun toDto(tag: Tag): Responses =
        Responses(
            id = tag.id,
            name = tag.name,
            type = tag.type.name
        )
}