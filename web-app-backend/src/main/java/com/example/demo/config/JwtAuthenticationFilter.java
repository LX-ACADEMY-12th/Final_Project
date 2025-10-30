package com.example.demo.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 모든 HTTP 요청마다 실행되는 필터입니다.
 * 요청 헤더(Authorization)에서 JWT 토큰을 추출하고,
 * 토큰이 유효하면 SecurityContext에 인증 정보를 저장합니다.
 * OncePerRequestFilter를 상속받아, 요청당 한 번만 실행되도록 보장합니다.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    // 생성자를 통해 JwtTokenProvider를 주입받습니다. (SecurityConfig에서 주입)
    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. 요청 헤더(Authorization)에서 토큰을 추출합니다.
        String token = resolveToken(request);

        // 2. 토큰이 존재하고, 유효한지(validateToken) 확인합니다.
        if (token != null && jwtTokenProvider.validateToken(token)) {

            // 3. 토큰이 유효하면, 토큰에서 인증 정보(Authentication)를 가져옵니다.
            Authentication authentication = jwtTokenProvider.getAuthentication(token);

            // 4. [중요] SecurityContextHolder에 인증 정보를 저장합니다.
            //    이제 이 요청은 Spring Security에 의해 '인증된' 요청으로 처리됩니다.
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        // 5. 다음 필터(예: Spring Security의 기본 필터)로 요청을 전달합니다.
        filterChain.doFilter(request, response);
    }

    /**
     * "Authorization" 헤더에서 "Bearer " 접두사를 제거하고 순수 토큰만 추출합니다.
     * @param request HttpServletRequest
     * @return "Bearer "가 제거된 토큰 문자열 (없거나 형식이 다르면 null)
     */
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // "Bearer " 7글자 이후의 토큰 값
        }
        return null;
    }
}

