package com.example.demo.config;

// 🟢 [추가] 필요한 import
import com.example.demo.config.JwtAuthenticationFilter;
import com.example.demo.config.JwtTokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy; // 🟢 세션 관리
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; // 🟢 암호화
import org.springframework.security.crypto.password.PasswordEncoder; // 🟢 암호화
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter; // 🟢 필터 추가

// 🟢 [추가] CORS 설정을 위한 Import
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.List; // 🟢 [추가]

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // 🟢 JwtTokenProvider 주입
    private final JwtTokenProvider jwtTokenProvider;

    public SecurityConfig(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    // 🟢 PasswordEncoder 빈 등록
    // UserService에서 비밀번호 암호화를 위해 사용합니다.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 🟢 [추가] CORS 설정을 filterChain에 적용합니다.
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 1. CSRF 비활성화 (Stateless JWT 사용)
                .csrf(csrf -> csrf.disable())

                // 2. 🟢 [중요] 세션 관리 정책을 STATELESS로 설정
                // (세션을 사용하지 않고, 모든 요청을 토큰 기반으로 처리)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 3. 🟢 요청별 접근 권한 설정
                .authorizeHttpRequests(authz -> authz
                        // User 컨트롤러 공개 API
                        .requestMatchers(
                                "/api/user/login",
                                "/api/user/signup",
                                "/api/user/check-id/**",
                                "/api/user/check-email/**",
                                "/api/token/refresh"
                        ).permitAll()

                        // Review 컨트롤러의 '조회(GET)' API는 공개
                        .requestMatchers(HttpMethod.GET,
                                "/api/reviews",
                                "/api/reviews/target/**",
                                "/api/reviews/photos-summary",
                                "/api/places/search"
                        ).permitAll()

                        // '인증'이 필요한 모든 API
                        .requestMatchers(
                                "/api/user/**",              // 내 정보, 수정, 탈퇴
                                "/api/reviews/liked-status", // 내 좋아요 확인
                                "/api/reviews/**",           // 리뷰 작성, 수정, 삭제, 좋아요, 신고
                                "/api/schedules/**"
                        ).authenticated()

                        // API 외의 요청(예: React 정적 파일 접근)이 있을 수 있으므로 변경
                        .anyRequest().permitAll()
                )

                // 4. 🟢 JwtAuthenticationFilter를 Spring Security 필터 체인에 추가
                // (UsernamePasswordAuthenticationFilter보다 먼저 실행되어야 함)
                .addFilterBefore(
                        new JwtAuthenticationFilter(jwtTokenProvider),
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    // 🟢 [추가] CORS 설정 Bean
    // 이 Bean을 추가하여 'http://localhost:5173' (Vue.js)로부터의 요청을 허용합니다.
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 1. [필수] 요청을 허용할 Origin (Vue.js 서버 주소)
        configuration.setAllowedOrigins(List.of("http://localhost:5173"));

        // 2. 허용할 HTTP 메서드 (GET, POST, PUT, DELETE, OPTIONS 등)
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // 3. 허용할 HTTP 헤더 (모든 헤더 허용)
        configuration.setAllowedHeaders(List.of("*"));

        // 4. [필수] 자격 증명(쿠키, Authorization 헤더 등)을 허용
        // (이것이 true여야 JWT 토큰을 헤더에 담아 보낼 수 있습니다)
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 5. 이 설정을 적용할 API 경로 (모든 /api/** 경로)
        source.registerCorsConfiguration("/api/**", configuration);

        return source;
    }
}