package kr.co.programmers.collabond.api.profile.application

import jakarta.persistence.criteria.*
import kr.co.programmers.collabond.api.file.application.FileService
import kr.co.programmers.collabond.api.image.application.ImageService
import kr.co.programmers.collabond.api.image.domain.Image
import kr.co.programmers.collabond.api.image.infrastructure.ImageMapper
import kr.co.programmers.collabond.api.profile.domain.Profile
import kr.co.programmers.collabond.api.profile.domain.ProfileType
import kr.co.programmers.collabond.api.profile.domain.dto.ProfileDetailResponseDto
import kr.co.programmers.collabond.api.profile.domain.dto.ProfileRequestDto
import kr.co.programmers.collabond.api.profile.domain.dto.ProfileResponseDto
import kr.co.programmers.collabond.api.profile.infrastructure.ProfileRepository
import kr.co.programmers.collabond.api.profile.interfaces.toDetailResponseDto
import kr.co.programmers.collabond.api.profile.interfaces.toEntity
import kr.co.programmers.collabond.api.profile.interfaces.toResponseDto
import kr.co.programmers.collabond.api.tag.application.TagService
import kr.co.programmers.collabond.api.user.application.UserService
import kr.co.programmers.collabond.core.auth.oauth2.OAuth2UserInfo
import kr.co.programmers.collabond.shared.exception.ErrorCode
import kr.co.programmers.collabond.shared.exception.ForbiddenException
import kr.co.programmers.collabond.shared.exception.InternalException
import kr.co.programmers.collabond.shared.exception.InvalidException
import kr.co.programmers.collabond.shared.exception.NotFoundException
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.util.*
import java.util.function.Consumer

@Service
class ProfileService(
    private val imageService: ImageService,
    private val fileService: FileService,
    private val userService: UserService,
    private val tagService: TagService,
    private val profileRepository: ProfileRepository
){
    @Transactional
    fun create(
        dto: ProfileRequestDto,
        profileImage: MultipartFile,
        thumbnailImage: MultipartFile,
        extraImages: List<MultipartFile>?,
        userInfo: OAuth2UserInfo
    ): ProfileResponseDto {
        val user = userService.findByProviderId(userInfo.username)

        if (!user.role.name.contains(dto.type)) {
            throw ForbiddenException()
        }

        if (profileRepository.countByUserId(user.id) >= 5) {
            throw InvalidException(ErrorCode.CREATE_NO_MORE)
        }

        // Profile 엔티티 생성, db에 저장 후 ResponseDto 반환
        val profile = toEntity(dto, user)

        saveImage(profile, profileImage, "PROFILE", 1)
        saveImage(profile, thumbnailImage, "THUMBNAIL", 1)

        if (!extraImages.isNullOrEmpty()) {
            for (i in extraImages.indices) {
                val file = extraImages[i]
                if (!file.isEmpty) {
                    saveImage(profile, file, "EXTRA", i + 1)
                }
            }
        }

        val savedProfile = profileRepository.save(profile)
        val tagIds = dto.tagIds

        if (tagIds.isNotEmpty()) {
            tagService.validateAndBindTags(savedProfile, tagIds)
        }

        return toResponseDto(savedProfile, getProfileImgName(savedProfile))
    }

    @Transactional
    fun update(
        profileId: Long,
        dto: ProfileRequestDto,
        profileImage: MultipartFile?,
        thumbnailImage: MultipartFile?,
        extraImages: List<MultipartFile>?
    ): ProfileResponseDto {
        val profile = profileRepository.findById(profileId)
            .orElseThrow {
                NotFoundException(
                    ErrorCode.PROFILE_NOT_FOUND
                )
            }

        profile.update(dto.name, dto.description, dto.addressCode, dto.address, dto.status) //활성/비활성 업데이트

        updateImage(profile, profileImage, "PROFILE")
        updateImage(profile, thumbnailImage, "THUMBNAIL")
        updateExtraImages(profile, extraImages)

        // 태그 재설정
        tagService.clearTags(profile) //기존 태그 모두 삭제 후
        tagService.validateAndBindTags(profile, dto.tagIds) //태그 최대 5개 등록 및 타입 검증

        val profileImg = getProfileImgName(profile)

        return toResponseDto(profile, profileImg)
    }

    @Transactional
    fun delete(profileId: Long) {
        val profile = profileRepository.findById(profileId)
            .orElseThrow {
                NotFoundException(
                    ErrorCode.PROFILE_NOT_FOUND
                )
            }

        // 파일은 hard delete
        profile.images.clear()

        // profile은 엔티티  @SQLDelete로 인해 soft delete 됨
        profileRepository.delete(profile)
    }

    // ID로 프로필 조회 후 존재할 경우 ResponseDto로 변환하여 반환
    fun findById(id: Long): Optional<ProfileResponseDto> {
        return profileRepository.findById(id)
            .map { profile: Profile ->
                toResponseDto(
                    profile,
                    getProfileImgName(profile)
                )
            }
    }

    // 특정 User의 모든 프로필 조회, 각 프로필을 ResponseDto로 매필해 반환
    fun findAllByUser(userId: Long): List<ProfileResponseDto> {
        return profileRepository.findAllByUserId(userId).stream()
            .map { profile: Profile -> toResponseDto(profile, getProfileImgName(profile)) }
            .toList()
    }


    // 이미지 저장 메서드
    private fun saveImage(
        profile: Profile,
        imageFile: MultipartFile,
        type: String,
        priority: Int
    ) {
        try {

            val file = fileService.saveFile(imageFile)
            val image = ImageMapper.toEntity(profile, file, type, priority)
            profile.addImage(image)
        } catch (e: RuntimeException) {
            throw InternalException(ErrorCode.SAVE_IMAGE_ERROR)
        }
    }

    //타입별 이미지 삭제 (파일 하드삭제 ,이미지 삭제)
    private fun deleteImagesByType(profile: Profile, type: String) {
        imageService.findByProfileIdAndType(profile.id, type)
            .forEach(Consumer { i: Image? -> profile.images.remove(i) })
    }

    private fun updateImage(profile: Profile, imageFile: MultipartFile?, type: String) {
        if (imageFile != null && !imageFile.isEmpty) {
            deleteImagesByType(profile, type)
            saveImage(profile, imageFile, type, 1)
        }
    }

    private fun updateExtraImages(profile: Profile, extraImages: List<MultipartFile>?) {
        if (!extraImages.isNullOrEmpty()) {
            deleteImagesByType(profile, "EXTRA")
            for (i in extraImages.indices) {
                val file = extraImages[i]
                if (!file.isEmpty) {
                    saveImage(profile, file, "EXTRA", i + 1)
                }
            }
        }
    }

    @Transactional
    fun findByProfileId(profileId: Long): Profile {
        return profileRepository.findById(profileId)
            .orElseThrow {
                NotFoundException(
                    ErrorCode.PROFILE_NOT_FOUND
                )
            }
    }

    private fun getProfileImgName(profile: Profile): String {
        val profileImage = profile.images.stream()
            .filter { i: Image -> i.type == "PROFILE" }
            .findAny()
            .orElse(null)
        return profileImage!!.file.savedName
    }

    fun searchProfiles(
        type: ProfileType,
        addressCodes: List<String>,
        tagIds: List<Long>,
        pageable: Pageable
    ): Page<ProfileDetailResponseDto> {
        val spec = Specification { root: Root<Profile?>, query: CriteriaQuery<*>?, cb: CriteriaBuilder ->
            val predicates: MutableList<Predicate> = ArrayList()
            // 1) 타입 필터
            predicates.add(cb.equal(root.get<Any>("type"), type))
            // 2) 상태 필터
            predicates.add(cb.isTrue(root.get("status")))

            // 3) addressCode prefix 필터 (OR)
            if (addressCodes.isNotEmpty()) {
                val orList = addressCodes.stream()
                    .map { code: String ->
                        cb.like(
                            root.get("addressCode"),
                            "$code%"
                        )
                    }
                    .toList()
                predicates.add(cb.or(*orList.toTypedArray<Predicate>()))
            }

            // 4) tagIds 필터 (JOIN + IN)
            if (tagIds.isNotEmpty()) {
                val tagJoin: Join<Profile, *> = root.join<Profile, Any>("tags")
                predicates.add(tagJoin.get<Any>("tag").get<Any>("id").`in`(tagIds))
                query!!.distinct(true) // JOIN 시 중복 제거
            }
            cb.and(*predicates.toTypedArray<Predicate>())
        }

        val page = profileRepository.findAll(spec, pageable)
        val dtos = page
            .map { profile ->
                toDetailResponseDto(profile)
            }
            .content
        return PageImpl(dtos, pageable, page.totalElements)
    }
}
