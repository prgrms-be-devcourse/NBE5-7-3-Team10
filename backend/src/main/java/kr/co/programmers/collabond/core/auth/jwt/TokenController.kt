package kr.co.programmers.collabond.core.auth.jwt

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/tokens")
class TokenController(
    private val tokenService: TokenService
) {
    @PostMapping("/refresh")
    fun refreshToken(@RequestBody request: RefreshTokenRequestDto):
            ResponseEntity<AccessTokenResponseDto> =
        ResponseEntity.ok(tokenService.refreshAccessToken(request.refreshToken))

    @PostMapping("/logout")
    fun logout(@RequestBody request: RefreshTokenRequestDto): ResponseEntity<Unit> {
        tokenService.logout(request.refreshToken)
        return ResponseEntity.ok().build()
    }
}