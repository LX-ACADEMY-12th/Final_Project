package com.example.demo.config;

// 🟢 [추가] 필요한 import
import com.example.demo.config.JwtTokenProvider;
import com.example.demo.config.JwtTokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy; // 🟢 세션 관리
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; // 🟢 암호화
import org.springframework.security.crypto.password.PasswordEncoder; // 🟢 암호화
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter; // 🟢 필터 추가

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // 🟢 [추가] JwtTokenProvider 주입
    private final JwtTokenProvider jwtTokenProvider;

    public SecurityConfig(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    // 🟢 [추가] PasswordEncoder 빈 등록
    // UserService에서 비밀번호 암호화를 위해 사용합니다.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. CSRF 비활성화 (Stateless JWT 사용)
                .csrf(csrf -> csrf.disable())

                // 2. 🟢 [중요] 세션 관리 정책을 STATELESS로 설정
                // (세션을 사용하지 않고, 모든 요청을 토큰 기반으로 처리)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 3. 🟢 [수정] 요청별 접근 권한 설정
                .authorizeHttpRequests(authz -> authz
                        // '/api/user/' 하위의 공개 API 경로들
                        .requestMatchers(
                                "/api/user/login",
                                "/api/user/signup",
                                "/api/user/check-id/**",
                                "/api/user/check-email/**"
                        ).permitAll() // 👈 이 경로들은 인증 없이 허용

                        // 🟢 [수정] 그 외 '/api/'로 시작하는 모든 요청은 인증이 필요함
                        .requestMatchers("/api/**").authenticated()

                        // 🔴 [기존] .anyRequest().authenticated()
                        // 🟢 [수정] API 외의 요청(예: React 정적 파일 접근)이 있을 수 있으므로 변경
                        .anyRequest().permitAll()
                )

                // 4. 🟢 [중요] JwtAuthenticationFilter를 Spring Security 필터 체인에 추가
                // (UsernamePasswordAuthenticationFilter보다 먼저 실행되어야 함)
                .addFilterBefore(
                        new JwtAuthenticationFilter(jwtTokenProvider),
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}
