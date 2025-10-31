package com.example.demo.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@Component
public class JwtTokenProvider {

    private final SecretKey key;
    private final long accessTokenValidityInMilliseconds;
    private final long refreshTokenValidityInMilliseconds;

    public JwtTokenProvider(
            @Value("${jwt.secret-key}") String secretKey,
            @Value("${jwt.access-token-expiration-ms}") long accessTokenValidityInMilliseconds,
            @Value("${jwt.refresh-token-expiration-ms}") long refreshTokenValidityInMilliseconds) {

        // 1. Base64로 인코딩된 비밀 키를 디코딩하여 SecretKey 객체로 변환
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        this.accessTokenValidityInMilliseconds = accessTokenValidityInMilliseconds;
        this.refreshTokenValidityInMilliseconds = refreshTokenValidityInMilliseconds;
    }

    // 2. 액세스 토큰 생성
    // (userId를 'subject'로 사용합니다)
    public String createAccessToken(int userId) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + accessTokenValidityInMilliseconds);

        return Jwts.builder()
                .subject(String.valueOf(userId)) // 사용자의 loginId 저장
                .issuedAt(now)
                .expiration(validity)
                .signWith(key)
                .compact();
    }

    // 3. 리프레시 토큰 생성
    public String createRefreshToken(int userId) { // <--- 1. int userId 매개변수 추가
        Date now = new Date();
        Date validity = new Date(now.getTime() + refreshTokenValidityInMilliseconds);

        return Jwts.builder()
                .subject(String.valueOf(userId)) // <--- 2. userId를 'subject'에 저장
                .issuedAt(now)
                .expiration(validity)
                .signWith(key)
                .compact();
    }

    // 4. 토큰 검증 (만료 여부, 서명 확인)
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            // (JwtException, SecurityException, MalformedJwtException, ExpiredJwtException 등)
            System.err.println("유효하지 않은 토큰입니다: " + e.getMessage());
            return false;
        }
    }

    // 5. 토큰에서 userId 추출
    public String getUserIdFromToken(String token) {

        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    // 6. 토큰에서 인증 정보(Authentication) 객체 생성
    // Spring Security가 이 객체를 사용하여 사용자를 인증합니다.
    public Authentication getAuthentication(String token) {
        String userId = getUserIdFromToken(token);
        System.out.println("[JwtTokenProvider] getAuthentication: 토큰에서 추출한 subject(userId): " + userId);
        // (간단한 구현) UserDetails 객체를 임시로 생성 (DB 연동 X)
        // 실제로는 userId로 UserService에서 UserDetails를 조회해야 합니다.
        // 여기서는 userId를 principal로, 빈 권한 목록을 사용합니다.
        UserDetails userDetails = new User(userId, "", List.of());

        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }
}