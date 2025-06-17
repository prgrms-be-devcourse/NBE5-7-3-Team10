package kr.co.programmers.collabond.core.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class CorsMvcConfig(
    @Value("\${custom.cors.allowed-origin}")
    private val allowedOrigin: String
) : WebMvcConfigurer {

    override fun addCorsMappings(corsRegistry: CorsRegistry) {
        corsRegistry.addMapping("/**")
            .allowedMethods("*")
            .allowCredentials(true)
            .allowedOrigins(allowedOrigin)
    }
}
