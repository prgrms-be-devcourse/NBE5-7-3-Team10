package kr.co.programmers.collabond.core.auth.oauth2

import kr.co.programmers.collabond.api.user.domain.Role
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.core.user.OAuth2User

data class OAuth2UserInfo(
    val username: String,
    val email: String = "",
    val _name: String = "",
    val role: Role,
    private val attributes: Map<String, Any> = emptyMap()
) : OAuth2User {

    override fun getAuthorities(): Collection<GrantedAuthority> =
        listOf(SimpleGrantedAuthority(role.name))

    override fun getAttributes(): Map<String, Any> = attributes

    override fun getName(): String = _name

    // providerId는 username과 동일
    val providerId: String get() = username
}