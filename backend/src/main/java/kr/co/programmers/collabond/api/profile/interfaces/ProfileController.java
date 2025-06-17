package kr.co.programmers.collabond.api.profile.interfaces;

import kr.co.programmers.collabond.api.profile.application.ProfileService;
import kr.co.programmers.collabond.api.profile.domain.ProfileType;
import kr.co.programmers.collabond.api.profile.domain.dto.ProfileDetailResponseDto;
import kr.co.programmers.collabond.api.profile.domain.dto.ProfileRequestDto;
import kr.co.programmers.collabond.api.profile.domain.dto.ProfileResponseDto;
import kr.co.programmers.collabond.core.auth.oauth2.OAuth2UserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/profiles")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    // 프로필 생성
    @PostMapping
    public ResponseEntity<ProfileResponseDto> create(
            @RequestPart("profileRequest") ProfileRequestDto request,
            @RequestPart(name = "profileImage", required = true) MultipartFile profileImage,
            @RequestPart(name = "thumbnailImage", required = true) MultipartFile thumbnailImage,
            @RequestPart(name = "extraImages", required = false) List<MultipartFile> extraImages,
            @AuthenticationPrincipal OAuth2UserInfo userInfo) {

        ProfileResponseDto response = profileService
                .create(request, profileImage, thumbnailImage, extraImages, userInfo);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    //프로필 수정
    @PatchMapping(
            value = "/{profileId}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ProfileResponseDto> update(
            @PathVariable Long profileId,
            @RequestPart("profileRequest") ProfileRequestDto request,
            @RequestPart(name = "profileImage", required = false) MultipartFile profileImage,
            @RequestPart(name = "thumbnailImage", required = false) MultipartFile thumbnailImage,
            @RequestPart(name = "extraImages", required = false) List<MultipartFile> extraImages) {

        ProfileResponseDto response = profileService
                .update(profileId, request, profileImage, thumbnailImage, extraImages);

        return ResponseEntity.ok(response);
    }

    //특정 프로필 id로 프로필 상세 조회
    @GetMapping("/{profileId}")
    public ResponseEntity<ProfileResponseDto> get(@PathVariable Long profileId) {
        return profileService.findById(profileId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    //특정 유저가 가진 모든 프로필 목록 조회
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ProfileResponseDto>> getByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(profileService.findAllByUser(userId));
    }

    //프로필 삭제시 연결된 파일은 HARDDELETE 후 프로필은 SOFTDELETE됨
    @DeleteMapping("/{profileId}")
    public ResponseEntity<Void> delete(@PathVariable Long profileId) {
        profileService.delete(profileId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<Page<ProfileDetailResponseDto>> searchProfiles(
            @RequestParam String type,
            @RequestParam(required = false) List<Long> tagIds,
            @RequestParam(required = false) List<String> addressCodes,
            Pageable pageable) {
        Page<ProfileDetailResponseDto> profiles = profileService.searchProfiles(
                ProfileType.valueOf(type.toUpperCase()), addressCodes, tagIds, pageable);

        return ResponseEntity.ok(profiles);
    }
}
