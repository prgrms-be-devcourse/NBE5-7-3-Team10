package kr.co.programmers.collabond.util;

import kr.co.programmers.collabond.api.user.domain.Role;
import kr.co.programmers.collabond.core.auth.oauth2.OAuth2UserInfo;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.util.Collections;

public class WithCustomMockUserSecurityContextFactory
        implements WithSecurityContextFactory<WithCustomMockUser> {

    @Override
    public SecurityContext createSecurityContext(WithCustomMockUser annotation) {
        String username = annotation.username();
        Role role = annotation.role();

        OAuth2UserInfo oAuth2UserInfo = new OAuth2UserInfo(username, "", "", role, Collections.emptyMap());

        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                oAuth2UserInfo, null, oAuth2UserInfo.getAuthorities());
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(token);
        return context;
    }
}
