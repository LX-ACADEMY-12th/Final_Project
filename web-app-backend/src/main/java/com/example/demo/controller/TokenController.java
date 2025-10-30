package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.config.JwtTokenProvider;
import com.example.demo.dto.TokenRefreshRequestDTO;
import com.example.demo.dto.TokenRefreshResponseDTO;

@RestController
@RequestMapping("/api/token") // URL 경로 /api/token
public class TokenController {

    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public TokenController(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     * @PostMapping("/refresh")
     * 프론트엔드(axios 인터셉터)에서 AccessToken이 만료(401)되었을 때,
     * 이 엔드포인트로 RefreshToken을 보내 새 AccessToken을 요청합니다.
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshAccessToken(@RequestBody TokenRefreshRequestDTO requestDTO) {

        String refreshToken = requestDTO.getRefreshToken();

        // 1. RefreshToken이 유효한지 검증 (validateToken)
        if (refreshToken != null && jwtTokenProvider.validateToken(refreshToken)) {

            try {
                // 2. RefreshToken에서 UserId 추출 (getUserIdFromToken)
                // (JwtTokenProvider.createRefreshToken(userId)로 만들었다는 전제)
                String userIdStr = jwtTokenProvider.getUserIdFromToken(refreshToken);
                int userId = Integer.parseInt(userIdStr);

                // 3. 새 AccessToken 생성
                String newAccessToken = jwtTokenProvider.createAccessToken(userId);

                // 4. 새 AccessToken을 DTO에 담아 200 OK 응답
                return ResponseEntity.ok(new TokenRefreshResponseDTO(newAccessToken));

            } catch (Exception e) {
                // (토큰 파싱 중 오류 발생 시)
                System.err.println("토큰 갱신 중 오류: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("리프레시 토큰 처리 중 오류가 발생했습니다.");
            }
        }

        // 5. RefreshToken이 없거나 유효하지 않은 경우
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("유효하지 않은 리프레시 토큰입니다.");
    }
}