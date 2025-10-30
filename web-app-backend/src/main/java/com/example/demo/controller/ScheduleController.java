package com.example.demo.controller;

import com.example.demo.dto.UserScheduleDTO;
import com.example.demo.service.ScheduleService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
// Spring Security 및 인증 관련 Import
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

// Logger 임포트
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Slf4j
@RestController
@RequestMapping("/api/schedules")
public class ScheduleController {

    private final ScheduleService scheduleService;

    // 생성자 주입
    public ScheduleController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    /**
     * 특정 사용자의 모든 최종 일정 및 상세 아이템 목록 조회
     * @param userId 사용자 ID
     * @return
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserSchedulesWithItems(
            @PathVariable("userId") Long userId,
            Authentication authentication
        )
    {

        // 토큰에서 인증된 사용자 ID 추출
        Long authUserId = getUserIdFromAuthentication(authentication);
        if (authUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("유효하지 않은 토큰입니다.");
        }

        // [보안 검사] 경로의 userId와 토큰의 authUserId가 일치하는지 확인
        if (!userId.equals(authUserId)) {
            log.warn("FORBIDDEN: 인증된 사용자(ID:{})가 다른 사용자(ID:{})의 일정 조회를 시도했습니다.", authUserId, userId);
            // 403 Forbidden (권한 없음) 반환
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("자신의 일정만 조회할 수 있습니다.");
        }

        // API 호출 로그
        log.info("ScheduleController - getUserSchedulesWithItems 호출됨. (Authenticated) userId: {}", userId);

        try {
            List<UserScheduleDTO> schedules = scheduleService.getSchedulesByUserId(userId);
            log.info("서비스 응답 완료. 조회된 스케줄 개수: {}", schedules.size());
            return ResponseEntity.ok(schedules); // List<UserScheduleDTO> 반환
        } catch (Exception e) {
            // 에러 발생 시 로그
            log.error("스케줄 조회 중 오류 발생. userId: {}", userId, e);
            // 에러 응답 반환 (예시)
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 🟢  Authentication 객체에서 'userId' (Long)를 추출하는 헬퍼 메서드
     * (다른 컨트롤러와 동일)
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