package com.example.demo.controller;

import com.example.demo.dto.ScheduleSaveRequestDTO;
import com.example.demo.service.ScheduleUpdateService;
import lombok.RequiredArgsConstructor; // Lombok 임포트
import lombok.extern.slf4j.Slf4j; // Slf4j (로깅) 임포트
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j // private static final Logger log = LoggerFactory.getLogger(ScheduleItemController.class); 자동 생성
@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor // final 필드에 대한 생성자 자동 주입
public class ScheduleUpdateController {

    private final ScheduleUpdateService scheduleUpdateService;

    /**
     * 스케줄에 속한 아이템 목록 전체를 저장 (덮어쓰기)
     */
    @PostMapping("/items")
    public ResponseEntity<String> saveScheduleItems(@RequestBody ScheduleSaveRequestDTO requestDto) {

        // {} 위치에 변수가 순서대로 매핑됩니다.
        log.info("API CALL: saveScheduleItems - scheduleId: {}", requestDto.getScheduleId());

        if (requestDto.getItems() != null) {
            log.info("Received {} items for scheduleId: {}", requestDto.getItems().size(), requestDto.getScheduleId());
        } else {
            log.warn("Received 0 items for scheduleId: {}", requestDto.getScheduleId());
        }

        try {
            scheduleUpdateService.saveScheduleItems(requestDto);
            log.info("Successfully saved items for scheduleId: {}", requestDto.getScheduleId());
            return ResponseEntity.ok("스케줄 아이템이 성공적으로 저장되었습니다.");

        } catch (Exception e) {
            // 에러 로그를 남길 때는 예외 객체(e)를 함께 넘겨주면 스택 트레이스가 출력됩니다.
            log.error("Error saving items for scheduleId: {}", requestDto.getScheduleId(), e);
            return ResponseEntity.badRequest().body("저장 중 오류 발생: " + e.getMessage());
        }
    }
}