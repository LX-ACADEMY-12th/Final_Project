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

    @Bean
    // @Order(2) // 필터 체인이 하나이므로 Order 불필요
    public SecurityFilterChain defaultfilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 💡 [추가] 인증/인가 예외 처리 핸들러 (유지)
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
                            response.getWriter().write("Unauthenticated: 유효한 토큰이 필요합니다.");
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN); // 403
                            response.getWriter().write("Forbidden: 해당 리소스에 접근할 권한이 없습니다.");
                        })
                )

                // 3. 🟢 요청별 접근 권한 설정 (순서 재정렬 및 수정)
                .authorizeHttpRequests(authz -> authz

                        // --- 1. 공개 API (permitAll) ---
                        // (누구나 접근 가능)
                        .requestMatchers(
                                // 사용자 인증 (회원가입/로그인/중복검사)
                                "/api/user/login",
                                "/api/user/signup",
                                "/api/user/check-id/**",
                                "/api/user/check-email/**",
                                "/api/token/refresh",

                                // 공지사항
                                "/api/notices/**",

                                // GET 요청만 공개
                                "/api/admin/reviews/**" // (GET 요청은 하단에서 별도 처리)
                        ).permitAll()

                        // HTTP Method 'GET'만 공개
                        .requestMatchers(HttpMethod.GET,
                                // 리뷰 조회
                                "/api/reviews",
                                "/api/reviews/target/**",
                                "/api/reviews/photos-summary",

                                // 장소 검색 및 조회
                                "/api/places/search",
                                "/api/halls/**",
                                "/api/centers/**",

                                // 추천 코스 조회
                                "/api/recommend/course"
                        ).permitAll()

                        // --- 2. 관리자 API (hasRole) ---
                        // (인증 + ADMIN 권한 필요)
                        // /api/admin/으로 시작하는 모든 요청은 ADMIN 권한 필요
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // --- 3. 나머지 모든 API (authenticated) ---
                        // (인증만 되면 누구나 접근 가능 - USER, ADMIN 등)
                        // 위에서 permitAll() 처리된 요청을 제외한
                        // "그 외 모든 요청"은 "인증"이 필요함.
                        .anyRequest().authenticated()
                )
                .addFilterBefore(
                        new JwtAuthenticationFilter(jwtTokenProvider),
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    // 🟢 CORS 설정 Bean (유지)
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:5173",
                "http://localhost:5174","https://burnished-form-472201-t1.web.app" ));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);

        return source;
    }
}