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

@Slf4j
@RestController
@RequestMapping("/api/schedules") // 또는 더 구체적인 경로 예: /api/recommendations
@RequiredArgsConstructor
public class RecommendedCourseSaveController {

    private final RecommendedCourseService recommendedCourseService;

    @PostMapping("/save-recommended") // 추천 코스 저장 API 엔드포인트
    public ResponseEntity<String> saveRecommendedCourse(@RequestBody RecommendedCourseSaveRequestDTO requestDto) {
        log.info("API CALL: saveRecommendedCourse - userId: {}", requestDto.getUserId());
        try {
            recommendedCourseService.saveRecommendedCourse(requestDto);
            log.info("추천 코스 저장 성공 - userId: {}", requestDto.getUserId());
            return ResponseEntity.ok("추천 코스가 성공적으로 저장되었습니다.");
        } catch (Exception e) {
            log.error("추천 코스 저장 실패 - userId: {}", requestDto.getUserId(), e);
            // 예외 유형에 따라 더 구체적인 오류 메시지 반환 고려
            return ResponseEntity.badRequest().body("추천 코스 저장 중 오류 발생: " + e.getMessage());
        }
    }
}