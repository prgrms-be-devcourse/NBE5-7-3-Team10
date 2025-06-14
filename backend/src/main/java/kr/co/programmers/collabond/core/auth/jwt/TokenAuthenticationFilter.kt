package kr.co.programmers.collabond.core.auth.jwt

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import kr.co.programmers.collabond.core.auth.oauth2.toOAuth2UserInfo
import kr.co.programmers.collabond.shared.exception.ErrorCode
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class TokenAuthenticationFilter(
    private val tokenService: TokenService
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        resolveToken(request)?.let { token ->
            if (tokenService.validate(token)) {
                tokenService.parseJwt(token).let { tokenBodyDto ->
                    val oAuth2UserInfo = toOAuth2UserInfo(tokenBodyDto)
                    val authentication = UsernamePasswordAuthenticationToken(
                        oAuth2UserInfo,
                        token,
                        oAuth2UserInfo.authorities
                    )
                    SecurityContextHolder.getContext().authentication = authentication
                }
            } else {
                response.status = ErrorCode.EXPIRED_TOKEN.status
                response.writer.write(ErrorCode.EXPIRED_TOKEN.message)
                return
            }
        }

        filterChain.doFilter(request, response)
    }

    private fun resolveToken(request: HttpServletRequest): String? =
        request.getHeader("Authorization")?.let { bearerToken ->
            if (bearerToken.startsWith("Bearer ")) {
                bearerToken.substring(7)
            } else null
        }
}