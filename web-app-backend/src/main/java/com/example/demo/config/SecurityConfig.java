package com.example.demo.config;


import jakarta.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;

    public SecurityConfig(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 관리자 API용 필터 체인 (먼저 실행: @Order(1))
    // 이 필터 체인은 JWT 필터를 포함하지 않습니다.
    @Bean
    @Order(1)
    public SecurityFilterChain adminFilterChain(HttpSecurity http) throws Exception {
        http
                // '/api/admin/contents' (POST용) 경로 추가
                .securityMatcher("/api/admin/contents", "/api/admin/contents/**", "/uploads/images/**")
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(authz -> authz
                        // 2. 이 경로들은 인증 없이 모두 허용
                        .anyRequest().permitAll()
                );
        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain defaultfilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                
                // 💡 [추가] 인증/인가 예외 처리 핸들러 추가
                .exceptionHandling(exceptionHandling -> exceptionHandling
                    // 인증되지 않은 사용자가 보호된 리소스에 접근할 때 (401 Unauthorized)
                    .authenticationEntryPoint((request, response, authException) -> {
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
                        response.getWriter().write("Unauthenticated: 유효한 토큰이 필요합니다.");
                    })
                    // 인증되었으나 권한이 없는 사용자가 접근할 때 (403 Forbidden)
                    .accessDeniedHandler((request, response, accessDeniedException) -> {
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN); // 403
                        response.getWriter().write("Forbidden: 해당 리소스에 접근할 권한이 없습니다.");
                    })
                )

                // 3. 🟢 요청별 접근 권한 설정
                .authorizeHttpRequests(authz -> authz
                        // 공지사항 호출
                        .requestMatchers("/api/notices/**",
                                "/api/admin/reviews/**").permitAll()

                        // 1. User 컨트롤러 공개 API
                        .requestMatchers(
                                "/api/user/login",
                                "/api/user/signup",
                                "/api/user/check-id/**",
                                "/api/user/check-email/**",
                                "/api/token/refresh"
                        ).permitAll()

                        // 2. Review 컨트롤러의 '조회(GET)' API는 공개
                        .requestMatchers(HttpMethod.GET,
                                "/api/reviews",
                                "/api/reviews/target/**",
                                "/api/reviews/photos-summary",
                                "/api/places/search"
                        ).permitAll()

                        // 3. 추천 코스 '조회'는 공개 (GET)
                        .requestMatchers(HttpMethod.GET, "/api/recommend/course").permitAll()

                        // 4. ✅ 추천 코스 '저장'은 인증 필수 (POST) - 명시적으로 추가
                        .requestMatchers(HttpMethod.POST, "/api/schedules/save-recommended").authenticated()

                        // 5. 나머지 schedules 관련 API도 인증 필수
                        .requestMatchers("/api/schedules/**").authenticated()

                        // 6. 리뷰 관련 인증 필요 API
                        .requestMatchers(
                                "/api/user/**",              // 내 정보, 수정, 탈퇴
                                "/api/reviews/liked-status", // 내 좋아요 확인
                                "/api/reviews/**",           // 리뷰 작성, 수정, 삭제, 좋아요, 신고
                                "/api/schedules/**",
                                "/api/wishlist/**"           // 찜
                        ).authenticated()

                        // 7. 사용자 정보 API
                        .requestMatchers("/api/user/**").authenticated()

                        // 8. 위시리스트(찜) API
                        .requestMatchers("/api/wishlist/**").authenticated()

                        // 9. 나머지 요청은 모두 허용
                        .anyRequest().permitAll()
                )
                .addFilterBefore(
                        new JwtAuthenticationFilter(jwtTokenProvider),
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    // 🟢 CORS 설정 Bean
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:5173","http://localhost:5174" ));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);

        return source;
    }
}
