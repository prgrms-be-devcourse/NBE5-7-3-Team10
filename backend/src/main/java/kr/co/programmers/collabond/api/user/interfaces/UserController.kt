package kr.co.programmers.collabond.api.user.interfaces

import jakarta.validation.Valid
import kr.co.programmers.collabond.api.user.application.UserService
import kr.co.programmers.collabond.api.user.domain.SignUpResponseDto
import kr.co.programmers.collabond.api.user.domain.UserResponseDto
import kr.co.programmers.collabond.api.user.domain.UserSignUpRequestDto
import kr.co.programmers.collabond.api.user.domain.UserUpdateRequestDto
import kr.co.programmers.collabond.core.auth.oauth2.OAuth2UserInfo
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService
) {
    @GetMapping
    fun getMyUserInfo(@AuthenticationPrincipal userInfo: OAuth2UserInfo): ResponseEntity<UserResponseDto> =
        ResponseEntity.ok(userService.findByProviderIdToDto(userInfo.username))

    @GetMapping("/{userId}")
    fun getUserInfo(@PathVariable userId: Long): ResponseEntity<UserResponseDto> =
        ResponseEntity.ok(userService.findById(userId))

    @PatchMapping("/signup")
    fun signup(
        @AuthenticationPrincipal userInfo: OAuth2UserInfo,
        @Valid @RequestBody dto: UserSignUpRequestDto
    ): ResponseEntity<SignUpResponseDto> =
        ResponseEntity.ok(userService.signup(userInfo.username, dto))

    @PatchMapping
    fun update(
        @AuthenticationPrincipal userInfo: OAuth2UserInfo,
        @Valid @RequestBody dto: UserUpdateRequestDto
    ): ResponseEntity<UserResponseDto> =
        ResponseEntity.ok(userService.update(userInfo.username, dto))

    @DeleteMapping
    fun delete(@AuthenticationPrincipal userInfo: OAuth2UserInfo): ResponseEntity<Void> {
        userService.delete(userInfo.username)
        return ResponseEntity.ok().build()
    }
}