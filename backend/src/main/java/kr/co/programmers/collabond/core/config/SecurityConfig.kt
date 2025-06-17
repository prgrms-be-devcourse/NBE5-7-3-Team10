package kr.co.programmers.collabond.core.config

import kr.co.programmers.collabond.core.auth.CustomAuthenticationEntryPoint
import kr.co.programmers.collabond.core.auth.jwt.TokenAuthenticationFilter
import kr.co.programmers.collabond.core.auth.oauth2.OAuth2SuccessHandler
import kr.co.programmers.collabond.core.auth.oauth2.OAuth2UserService
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsUtils

@Configuration
@EnableWebSecurity
class SecurityConfig(
    @Value("\${custom.cors.allowed-origin}")
    private val allowedOrigin: String,
    private val oAuth2UserService: OAuth2UserService,
    private val oAuth2SuccessHandler: OAuth2SuccessHandler,
    private val entryPoint: CustomAuthenticationEntryPoint,
    private val tokenAuthenticationFilter: TokenAuthenticationFilter
) {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain = http
        .cors { cors ->
            cors.configurationSource {
                CorsConfiguration().apply {
                    allowedOrigins = listOf(allowedOrigin)
                    allowedMethods = listOf("*")
                    allowCredentials = true
                    allowedHeaders = listOf("*")
                    maxAge = 3600L
                    exposedHeaders = listOf("Set-Cookie", "Authorization")
                }
            }
        }
        .csrf { it.disable() }
        .httpBasic { it.disable() }
        .formLogin { it.disable() }
        .logout { it.disable() }
        .addFilterAfter(
            tokenAuthenticationFilter,
            UsernamePasswordAuthenticationFilter::class.java
        )
        .oauth2Login {
            it.userInfoEndpoint { userInfo ->
                userInfo.userService(oAuth2UserService)
            }
            it.successHandler(oAuth2SuccessHandler)
        }
        .authorizeHttpRequests { auth ->
            auth.requestMatchers(CorsUtils::isPreFlightRequest).permitAll()
            auth.requestMatchers("/admin/**").hasRole("ADMIN")
            auth.requestMatchers(HttpMethod.PATCH,"/api/users/signup").hasRole("TMP")
            auth.requestMatchers(HttpMethod.GET, "/api/users/{userId}",
                "/api/profiles/**",
                "/api/recruitments/**",
                "/api/tags/**").permitAll()
            auth.requestMatchers("/", "/error", "/oauth2/**", "/api/tokens/refresh").permitAll()
            auth.anyRequest().authenticated()
        }
        .exceptionHandling { handler -> handler.authenticationEntryPoint(entryPoint) }
        .sessionManagement {
            it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        }
        .build()
}