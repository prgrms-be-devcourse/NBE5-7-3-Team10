package kr.co.programmers.collabond.core.config;

import kr.co.programmers.collabond.core.auth.CustomAuthenticationEntryPoint;
import kr.co.programmers.collabond.core.auth.oauth2.OAuth2SuccessHandler;
import kr.co.programmers.collabond.core.auth.oauth2.OAuth2UserService;
import kr.co.programmers.collabond.core.auth.jwt.TokenAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsUtils;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Value("${custom.cors.allowed-origin}")
    private String allowedOrigin;

    private final OAuth2UserService oAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final CustomAuthenticationEntryPoint entryPoint;
    private final TokenAuthenticationFilter tokenAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(cors -> cors.configurationSource(
                        request -> {
                            CorsConfiguration configuration = new CorsConfiguration();

                            configuration.setAllowedOrigins(List.of(allowedOrigin));
                            configuration.setAllowedMethods(List.of("*"));
                            configuration.setAllowCredentials(true);
                            configuration.setAllowedHeaders(List.of("*"));
                            configuration.setMaxAge(3600L);

                            configuration.setExposedHeaders(List.of("Set-Cookie", "Authorization"));
                            return configuration;
                        }
                ))
                .csrf(csrf -> csrf.disable()) // csrf 비활성
                .httpBasic(AbstractHttpConfigurer::disable) // 기본 인증 로그인 비활성
                .formLogin(form -> form.disable()) // 기본 로그인 폼 비활성
                .logout(AbstractHttpConfigurer::disable) // 기본 로그아웃 비활성
                .addFilterAfter(tokenAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class)
                .oauth2Login(oauth -> {
                    oauth.userInfoEndpoint(userInfo ->
                                    userInfo.userService(oAuth2UserService))
                            .successHandler(oAuth2SuccessHandler);
                })
                // 임시로 Admin만 권한 검사
                .authorizeHttpRequests(
                        auth -> auth
                                .requestMatchers(CorsUtils::isPreFlightRequest).permitAll()
                                .requestMatchers("/admin/**").hasRole("ADMIN")
                                .requestMatchers(HttpMethod.PATCH,"/api/users/signup").hasRole("TMP")
                                .requestMatchers(HttpMethod.GET, "/api/users/{userId}",
                                    "/api/profiles/**",
                                    "/api/recruitments/**",
                                    "/api/tags/**").permitAll()
                                .requestMatchers("/",
                                        "/error",
                                        "/oauth2/**",
                                        "/api/tokens/refresh").permitAll()
                                .anyRequest().authenticated()
                )
                .exceptionHandling(handler -> handler.authenticationEntryPoint(entryPoint))
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 세션 사용 X
                .build();
    }
}
