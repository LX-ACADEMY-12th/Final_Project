package com.example.demo.controller;

import com.example.demo.dto.ActivityRecommendDTO;
import com.example.demo.service.ActivityRecommendService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/recommend")
@RequiredArgsConstructor
public class ActivityRecommendController {

    private final ActivityRecommendService activityRecommendService;

    /**
     * 장소 상세 페이지에서 AI 맞춤 체험활동을 추천받습니다.
     *
     * @param placeId 장소 ID
     * @param grade   학년 (숫자)
     * @param unit    교과 단원 (e.g., "물리")
     * @return 추천 활동 목록 (JSON Array)
     */
    @GetMapping("/activities")
    public ResponseEntity<List<ActivityRecommendDTO.Response>> recommendActivities(
            @RequestParam("placeId") Long placeId,
            @RequestParam("grade") Integer grade,
            @RequestParam("unit") String unit
    ) {
        // 1. 요청 파라미터를 DTO로 캡슐화
        ActivityRecommendDTO.Request requestDto = new ActivityRecommendDTO.Request(placeId, grade, unit);

        // 2. 서비스 호출
        // (예외 처리는 @ControllerAdvice에서 처리한다고 가정)
        List<ActivityRecommendDTO.Response> recommendations = activityRecommendService.getRecommendations(requestDto);

        // 3. 결과 반환
        return ResponseEntity.ok(recommendations);
    }
}