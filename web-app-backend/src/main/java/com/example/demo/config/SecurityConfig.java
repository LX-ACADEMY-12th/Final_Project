package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. CSRF 보호 비활성화 (API 서버는 stateless하므로 보통 비활성화합니다)
                .csrf(csrf -> csrf.disable())

                // 2. 요청별 접근 권한 설정
                .authorizeHttpRequests(authz -> authz
                        // '/api/' 로 시작하는 모든 요청은 인증 없이 허용
                        .requestMatchers("/api/**").permitAll()
                        // 그 외 나머지 모든 요청은 반드시 인증이 필요함
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}