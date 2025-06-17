package kr.co.programmers.collabond.api.profile.interfaces

import kr.co.programmers.collabond.api.profile.domain.Profile
import kr.co.programmers.collabond.api.profile.domain.ProfileType
import kr.co.programmers.collabond.api.profile.domain.dto.ProfileDetailResponseDto
import kr.co.programmers.collabond.api.profile.domain.dto.ProfileDto
import kr.co.programmers.collabond.api.profile.domain.dto.ProfileRequestDto
import kr.co.programmers.collabond.api.profile.domain.dto.ProfileResponseDto
import kr.co.programmers.collabond.api.profile.domain.dto.ProfileSimpleResponseDto
import kr.co.programmers.collabond.api.tag.interfaces.TagMapper
import kr.co.programmers.collabond.api.user.domain.User

fun toEntity(dto: ProfileRequestDto, user: User) = Profile(
    user = user,
    addressCode = dto.addressCode!!,
    address = dto.address!!,
    type = ProfileType.valueOf(dto.type),
    name = dto.name,
    description = dto.description,
    collaboCount = 0,
    status = true
)

fun toDto(entity: Profile) = ProfileDto(
    id = entity.id,
    name = entity.name,
    description = entity.description,
    type = entity.type.name,
    imageUrl = entity.images
        .firstOrNull { it.type == "PROFILE" }
        ?.file
        ?.savedName
        ?: "",
    collaboCount = entity.collaboCount,
    userId       = entity.user.id
)

fun toResponseDto(entity: Profile, imageUrl: String?) = ProfileResponseDto(
    id = entity.id,
    userId = entity.user.id,
    type = entity.type.name,
    name = entity.name,
    profileImageUrl = entity.images
        .firstOrNull { it.type == "PROFILE" }
        ?.file
        ?.savedName,
    thumbnailImageUrl = entity.images
        .firstOrNull { it.type == "THUMBNAIL" }
        ?.file
        ?.savedName,
    extraImageUrls = entity.images
        .filter { it.type != "PROFILE" && it.type != "THUMBNAIL" }
        .map   { it.file.savedName },
    description = entity.description,
    addressCode  = entity.addressCode,
    address = entity.address,
    collaboCount = entity.collaboCount,
    status = entity.status,
    tags = entity.tags.map { TagMapper.toDto(it.tag) },
    createdAt = entity.createdAt,
    updatedAt = entity.updatedAt!!
)

fun toSimpleDto(entity: Profile) = entity.images
    .firstOrNull { it.type == "PROFILE" }
    ?.file?.let {
        ProfileSimpleResponseDto(
            profileId = entity.id,
            imageUrl = it
                .savedName,
            type = entity.type.name,
            address = entity.address,
            status = TODO()
        )
    }

fun toDetailResponseDto(profile: Profile) = ProfileDetailResponseDto(
    id = profile.id,
    userId = profile.user.id,
    nickname = profile.user.nickname,
    type = profile.type.name,
    name = profile.name,
    profileImageUrl = profile.images
        .firstOrNull { it.type == "PROFILE" }
        ?.file
        ?.savedName,
    thumbnailImageUrl = profile.images
        .firstOrNull { it.type == "THUMBNAIL" }
        ?.file
        ?.savedName,
    extraImageUrls = profile.images
        .filter { it.type != "PROFILE" && it.type != "THUMBNAIL" }
        .map { it.file.savedName },
    description = profile.description,
    address = profile.address,
    addressCode = profile.addressCode,
    collaboCount = profile.collaboCount,
    status = profile.status,
    tags = profile.tags.map { TagMapper.toDto(it.tag) },
    createdAt = profile.createdAt,
    updatedAt = profile.updatedAt!!
)
