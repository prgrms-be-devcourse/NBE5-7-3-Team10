package kr.co.programmers.collabond.util

import kr.co.programmers.collabond.core.auth.oauth2.OAuth2UserInfo
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.test.context.support.WithSecurityContextFactory

class WithCustomMockUserSecurityContextFactory :
    WithSecurityContextFactory<WithCustomMockUser> {

    override fun createSecurityContext(annotation: WithCustomMockUser): SecurityContext {
        val username = annotation.username
        val role = annotation.role

        val oAuth2UserInfo = OAuth2UserInfo(username = username, role = role)

        val token = UsernamePasswordAuthenticationToken(
            oAuth2UserInfo,
            null,
            oAuth2UserInfo.authorities
        )

        return SecurityContextHolder.getContext().apply {
            authentication = token
        }
    }
}