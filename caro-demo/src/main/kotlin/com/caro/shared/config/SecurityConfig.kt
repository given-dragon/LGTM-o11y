package com.caro.shared.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.observation.SecurityObservationSettings
import org.springframework.security.web.SecurityFilterChain

/**
 * 개발용 Security 설정.
 * 모든 요청을 허용하고 CSRF를 비활성화합니다.
 */
@Configuration
@EnableWebSecurity
class SecurityConfig {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .csrf { it.disable() }
            .authorizeHttpRequests { auth ->
                auth.anyRequest().permitAll()
            }
            .build()
    }

    @Bean
    fun securityObservationSettings(): SecurityObservationSettings {
        return SecurityObservationSettings.noObservations()
    }
}