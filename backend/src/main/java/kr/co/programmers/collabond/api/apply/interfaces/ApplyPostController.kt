package kr.co.programmers.collabond.api.apply.interfaces

import jakarta.validation.Valid
import kr.co.programmers.collabond.api.apply.application.ApplyPostService
import kr.co.programmers.collabond.api.apply.domain.dto.ApplyPostDto
import kr.co.programmers.collabond.api.apply.domain.dto.ApplyPostRequestDto
import kr.co.programmers.collabond.api.apply.domain.dto.ReceivedApplyPostsRequestDto
import kr.co.programmers.collabond.api.apply.domain.dto.SentApplyPostsRequestDto
import kr.co.programmers.collabond.core.auth.oauth2.OAuth2UserInfo
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile


@RestController
@RequestMapping("/api/applications")
class ApplyPostController(
    private val applyPostService: ApplyPostService
) {

    @PostMapping("/{recruitmentId}")
    fun applyPost(
        @PathVariable recruitmentId: Long,
        @RequestPart("applyRequest") @Valid request: ApplyPostRequestDto,
        @RequestPart(name = "attachment", required = false) files: List<MultipartFile>?
    ): ResponseEntity<ApplyPostDto> {
        val response = applyPostService.applyPost(recruitmentId, request, files)

        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    // 내가 보낸 지원서
    @GetMapping("/sent")
    fun sentApplyPosts(
        request: SentApplyPostsRequestDto,
        @AuthenticationPrincipal userInfo: OAuth2UserInfo,
        @PageableDefault pageable: Pageable
    ): ResponseEntity<Page<ApplyPostDto>> {
        val applyPosts = applyPostService
            .findSentApplyPosts(request, userInfo, pageable)

        return ResponseEntity.ok(applyPosts)
    }

    // 내가 받은 지원서
    @GetMapping("/received")
    fun receivedApplyPosts(
        request: ReceivedApplyPostsRequestDto,
        @AuthenticationPrincipal userInfo: OAuth2UserInfo,
        @PageableDefault pageable: Pageable
    ): ResponseEntity<Page<ApplyPostDto>> {
        val applyPosts = applyPostService
            .findReceivedApplyPosts(request, userInfo, pageable)

        return ResponseEntity.ok(applyPosts)
    }

    @PatchMapping("/accept/{applicationId}")
    fun acceptApply(
        @PathVariable applicationId: Long,
        @AuthenticationPrincipal userInfo: OAuth2UserInfo
    ): ResponseEntity<Void> {
        applyPostService.acceptApply(applicationId, userInfo)

        return ResponseEntity.ok().build()
    }
}
