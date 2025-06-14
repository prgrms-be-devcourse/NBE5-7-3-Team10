package kr.co.programmers.collabond.core.auth.oauth2

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import kr.co.programmers.collabond.core.auth.jwt.LoginTokenResponseDto
import kr.co.programmers.collabond.core.auth.jwt.TokenService
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder

@Component
class OAuth2SuccessHandler(
    private val tokenService: TokenService,
    @Value("\${custom.jwt.redirect-login-success}")
    private val redirectSuccess: String
) : AuthenticationSuccessHandler {

    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        val oAuth2UserInfo = authentication.principal as OAuth2UserInfo

        val dto = tokenService.issueTokens(
            providerId = oAuth2UserInfo.username,
            role = oAuth2UserInfo.role
        )

        val targetUrl = buildRedirectUrl(dto)
        response.sendRedirect(targetUrl)
    }

    private fun buildRedirectUrl(dto: LoginTokenResponseDto) = UriComponentsBuilder
        .fromUriString(redirectSuccess)
        .apply {
            queryParam("accessToken", dto.accessToken)
            queryParam("refreshToken", dto.refreshToken)
            queryParam("userId", dto.id)
            queryParam("nickname", dto.nickname)
            queryParam("role", dto.role)
        }
        .build()
        .encode()
        .toUriString()
}