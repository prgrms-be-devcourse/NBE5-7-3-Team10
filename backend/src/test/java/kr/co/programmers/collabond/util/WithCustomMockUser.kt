package kr.co.programmers.collabond.util

import kr.co.programmers.collabond.api.user.domain.Role
import org.springframework.security.test.context.support.WithSecurityContext

@Retention(AnnotationRetention.RUNTIME)
@WithSecurityContext(factory = WithCustomMockUserSecurityContextFactory::class)
annotation class WithCustomMockUser(
    val username: String = "providerId",
    val role: Role = Role.ROLE_IP
)