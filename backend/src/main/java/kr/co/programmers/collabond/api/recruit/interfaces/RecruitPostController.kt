package kr.co.programmers.collabond.api.recruit.interfaces

import jakarta.validation.Valid
import kr.co.programmers.collabond.api.recruit.application.RecruitPostService
import kr.co.programmers.collabond.api.recruit.domain.RecruitPostStatus
import kr.co.programmers.collabond.api.recruit.domain.dto.Requests
import kr.co.programmers.collabond.api.recruit.domain.dto.Responses
import kr.co.programmers.collabond.core.auth.oauth2.OAuth2UserInfo
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/recruitments")
class RecruitPostController(
    private val recruitPostService: RecruitPostService
) {

    @PostMapping
    fun createRecruitPost(
        @Valid @RequestBody request: Requests,
        @AuthenticationPrincipal userInfo: OAuth2UserInfo
    ): ResponseEntity<Responses> =
        ResponseEntity.status(HttpStatus.CREATED)
            .body(recruitPostService.createRecruitPost(request, userInfo))

    @GetMapping("/{recruitmentId}")
    fun getRecruitPostById(@PathVariable recruitmentId: Long): ResponseEntity<Responses> =
        ResponseEntity.ok(recruitPostService.getRecruitPostById(recruitmentId))

    @PatchMapping("/{recruitmentId}")
    fun updateRecruitPost(
        @PathVariable recruitmentId: Long,
        @Valid @RequestBody request: Requests,
        @AuthenticationPrincipal userInfo: OAuth2UserInfo
    ): ResponseEntity<Responses> =
        ResponseEntity.ok(recruitPostService.updateRecruitPost(recruitmentId, request, userInfo))

    @DeleteMapping("/{recruitmentId}")
    fun deleteRecruitPost(
        @PathVariable recruitmentId: Long,
        @AuthenticationPrincipal userInfo: OAuth2UserInfo
    ): ResponseEntity<Void> {
        recruitPostService.deleteRecruitPost(recruitmentId, userInfo)
        return ResponseEntity.ok().build()
    }

    @GetMapping
    fun getAllRecruitPosts(
        @RequestParam(required = false) status: RecruitPostStatus? = null,
        @RequestParam(required = false) sort: String? = null,
        @PageableDefault(size = 10) pageable: Pageable
    ): ResponseEntity<Page<Responses>> =
        ResponseEntity.ok(recruitPostService.getAllRecruitPosts(status, sort, pageable))

    @GetMapping("/users/{userId}")
    fun getRecruitPostsByUser(
        @PathVariable userId: Long,
        @PageableDefault(size = 10) pageable: Pageable
    ): ResponseEntity<Page<Responses>> =
        ResponseEntity.ok(recruitPostService.getRecruitPostsByUser(userId, pageable))

    @GetMapping("/profiles/{profileId}")
    fun getRecruitPostByProfile(
        @PathVariable profileId: Long,
        @PageableDefault(size = 10) pageable: Pageable
    ): ResponseEntity<Page<Responses>> =
        ResponseEntity.ok(recruitPostService.getRecruitPostByProfile(profileId, pageable))
}