package kr.co.programmers.collabond.api.profile.interfaces

import kr.co.programmers.collabond.api.profile.application.ProfileService
import kr.co.programmers.collabond.api.profile.domain.ProfileType
import kr.co.programmers.collabond.api.profile.domain.dto.ProfileDetailResponseDto
import kr.co.programmers.collabond.api.profile.domain.dto.ProfileRequestDto
import kr.co.programmers.collabond.api.profile.domain.dto.ProfileResponseDto
import kr.co.programmers.collabond.core.auth.oauth2.OAuth2UserInfo
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.util.*

@RestController
@RequestMapping("/api/profiles")
class ProfileController (
    private val profileService: ProfileService
){
    // 프로필 생성
    @PostMapping
    fun create(
        @RequestPart("profileRequest") request: ProfileRequestDto,
        @RequestPart(name = "profileImage", required = true) profileImage: MultipartFile?,
        @RequestPart(name = "thumbnailImage", required = true) thumbnailImage: MultipartFile?,
        @RequestPart(name = "extraImages", required = false) extraImages: List<MultipartFile?>?,
        @AuthenticationPrincipal userInfo: OAuth2UserInfo
    ): ResponseEntity<ProfileResponseDto> {
        val response = profileService
            .create(request, profileImage, thumbnailImage, extraImages, userInfo)

        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    //프로필 수정
    @PatchMapping(value = ["/{profileId}"])
    fun update(
        @PathVariable profileId: Long?,
        @RequestPart("profileRequest") request: ProfileRequestDto,
        @RequestPart(name = "profileImage", required = false) profileImage: MultipartFile?,
        @RequestPart(name = "thumbnailImage", required = false) thumbnailImage: MultipartFile?,
        @RequestPart(name = "extraImages", required = false) extraImages: List<MultipartFile?>?
    ): ResponseEntity<ProfileResponseDto> {
        val response = profileService
            .update(profileId, request, profileImage, thumbnailImage, extraImages)

        return ResponseEntity.ok(response)
    }

    //특정 프로필 id로 프로필 상세 조회
    @GetMapping("/{profileId}")
    fun get(@PathVariable profileId: Long?): ResponseEntity<ProfileResponseDto> {
        return profileService.findById(profileId)
            .map<ResponseEntity<ProfileResponseDto>> { body: ProfileResponseDto? -> ResponseEntity.ok(body) }
            .orElse(ResponseEntity.notFound().build())
    }

    //특정 유저가 가진 모든 프로필 목록 조회
    @GetMapping("/user/{userId}")
    fun getByUser(@PathVariable userId: Long?): ResponseEntity<List<ProfileResponseDto>> {
        return ResponseEntity.ok(profileService.findAllByUser(userId))
    }

    //프로필 삭제시 연결된 파일은 HARDDELETE 후 프로필은 SOFTDELETE됨
    @DeleteMapping("/{profileId}")
    fun delete(@PathVariable profileId: Long?): ResponseEntity<Void> {
        profileService.delete(profileId)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/search")
    fun searchProfiles(
        @RequestParam type: String,
        @RequestParam(required = false) tagIds: List<Long?>?,
        @RequestParam(required = false) addressCodes: List<String?>?,
        pageable: Pageable?
    ): ResponseEntity<Page<ProfileDetailResponseDto>> {
        val profiles = profileService.searchProfiles(
            ProfileType.valueOf(type.uppercase(Locale.getDefault())), addressCodes, tagIds, pageable
        )

        return ResponseEntity.ok(profiles)
    }
}
