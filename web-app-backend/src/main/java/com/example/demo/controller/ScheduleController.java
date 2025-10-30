package com.example.demo.controller;

import com.example.demo.dto.UserScheduleDTO;
import com.example.demo.service.ScheduleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Logger 임포트
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/schedules")
public class ScheduleController {
    // Logger 필드 선언
    private static final Logger log = LoggerFactory.getLogger(ScheduleController.class);

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
    public ResponseEntity<List<UserScheduleDTO>> getUserSchedulesWithItems(
            @PathVariable("userId") Long userId) {

        // API 호출 로그 (파라미터 포함)
        log.info("ScheduleController - getUserSchedulesWithItems 호출됨. userId: {}", userId);

        List<UserScheduleDTO> schedules;

        try {
            schedules = scheduleService.getSchedulesByUserId(userId);

            // 서비스 응답 로그
            log.info("서비스 응답 완료. 조회된 스케줄 개수: {}", schedules.size());

        } catch (Exception e) {
            // 에러 발생 시 로그
            log.error("스케줄 조회 중 오류 발생. userId: {}", userId, e);
            // 에러 응답 반환 (예시)
            return ResponseEntity.internalServerError().build();
        }

        return ResponseEntity.ok(schedules);
    }
}