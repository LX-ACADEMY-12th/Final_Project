package com.example.demo.controller;

import com.example.demo.dto.CourseItemDTO;
import com.example.demo.service.RecommendService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recommend")
public class RecommendController {

    private final RecommendService recommendService;

    // RecommendService를 주입받습니다.
    public RecommendController(RecommendService recommendService) {
        this.recommendService = recommendService;
    }

    @GetMapping("/course")
    public ResponseEntity<List<CourseItemDTO>> getRecommendedCourse(
            @RequestParam String type,
            @RequestParam Long currentId,
            @RequestParam String mainCategoryTags,
            @RequestParam(required = false) String subCategoryTags, // 프론트와 키 이름 일치
            @RequestParam String gradeTags
    ) {
        // 파라미터가 잘 들어왔는지 확인
        System.out.println("--- API 호출: /api/recommend/course ---");
        System.out.println("type: " + type);
        System.out.println("currentId: " + currentId);
        System.out.println("mainCategoryTags: " + mainCategoryTags);
        System.out.println("subCategories: " + subCategoryTags); // null이 아닐 때만 값이 나옴
        System.out.println("gradeTags: " + gradeTags);
        System.out.println("----------------------------------------");
        // Service에게 모든 작업을 위임합니다.
        List<CourseItemDTO> recommendations = recommendService.getRecommendations(
                type, currentId, mainCategoryTags, subCategoryTags, gradeTags
        );

        // Service가 만든 결과를 프론트엔드에 반환합니다.
        return ResponseEntity.ok(recommendations);
    }
}