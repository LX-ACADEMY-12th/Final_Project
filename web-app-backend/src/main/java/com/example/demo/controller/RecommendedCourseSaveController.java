package com.example.demo.controller;

import com.example.demo.dto.RecommendedCourseSaveRequestDTO;
import com.example.demo.service.RecommendedCourseService; // 서비스 이름 가정
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Spring Security 인증 객체
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

@Slf4j
@RestController
@RequestMapping("/api/schedules") // 또는 더 구체적인 경로 예: /api/recommendations
@RequiredArgsConstructor
public class RecommendedCourseSaveController {

    private final RecommendedCourseService recommendedCourseService;

    @PostMapping("/save-recommended") // 추천 코스 저장 API 엔드포인트
    public ResponseEntity<String> saveRecommendedCourse(
        // 🟢 Spring Security가 토큰을 검증하고 주입해주는 인증 객체
        Authentication authentication,
        // 🟢 DTO에는 이제 userId가 없거나, 있어도 무시합니다.
        @RequestBody RecommendedCourseSaveRequestDTO requestDto
    ) {
        // 🟢 인증 객체에서 '진짜' userId를 추출합니다.
        Long userId = getUserIdFromAuthentication(authentication);
        if (userId == null) {
            return ResponseEntity.status(401).body("유효하지 않은 토큰입니다.");
        }

        log.info("API CALL: saveRecommendedCourse - (Authenticated) userId: {}", userId);

        try {
            recommendedCourseService.saveRecommendedCourse(requestDto, userId);
            log.info("추천 코스 저장 성공 - userId: {}", userId );
            return ResponseEntity.ok("추천 코스가 성공적으로 저장되었습니다.");
        } catch (Exception e) {
            log.error("추천 코스 저장 실패 - userId: {}", userId, e);
            // 예외 유형에 따라 더 구체적인 오류 메시지 반환 고려
            return ResponseEntity.badRequest().body("추천 코스 저장 중 오류 발생: " + e.getMessage());
        }
    }

    /**
     * 🟢  Authentication 객체에서 'userId' (Long)를 추출하는 헬퍼 메서드
     * (UserController, ReviewController와 동일한 메서드)
     */
    private Long getUserIdFromAuthentication(Authentication authentication) {
        if (authentication == null) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            // JwtTokenProvider가 토큰의 'subject'에 userId를 (String으로) 저장했습니다.
            String userIdStr = ((UserDetails) principal).getUsername();
            try {
                return Long.parseLong(userIdStr);
            } catch (NumberFormatException e) {
                log.error("Authentication principal이 Long 타입으로 변환 불가: {}", userIdStr, e);
                return null;
            }
        } else if (principal instanceof String) {
            // (대체 케이스)
            try {
                return Long.parseLong((String) principal);
            } catch (NumberFormatException e) {
                log.error("Authentication principal이 Long 타입으로 변환 불가: {}", principal, e);
                return null;
            }
        }

        log.warn("알 수 없는 Principal 타입: {}", principal.getClass().getName());
        return null;
    }
}