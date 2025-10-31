package com.example.demo.config;

import com.example.demo.config.JwtAuthenticationFilter;
import com.example.demo.config.JwtTokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(authz -> authz
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
    // 이 Bean을 추가하여 'http://localhost:5173' (Vue.js)로부터의 요청을 허용합니다.
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:5173"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);

        return source;
    }
}
