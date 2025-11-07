package com.example.demo.service;

import com.example.demo.dto.CourseHallDTO;
import com.example.demo.dto.CourseItemDTO; // [!!] DTO만 import
import com.example.demo.mapper.ExhibitionMapper;
import com.example.demo.mapper.ContentMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RecommendService {

    private final ContentMapper contentMapper;

    private final LlmApiService llmApiService; //

    public RecommendService(ExhibitionMapper exhibitionMapper, ContentMapper contentMapper, LlmApiService llmApiService) {
        this.contentMapper = contentMapper;
        this.llmApiService = llmApiService;
    }

    public List<CourseItemDTO> getRecommendations(
            String type, Long currentId, String mainCategory, String subCategories, String grade
    ) {

        if ("exhibition".equals(type)) {
            // AI에게 추천할 전시관 '후보'
            List<CourseHallDTO> candidates = contentMapper.findSimilarExhibition(currentId, mainCategory, grade);
            // AI가 추천한 전시관
            List<CourseHallDTO> recommendations = llmApiService.getAiRecommendations(currentId, candidates);
            // AI가 추천한 '전시관 ID' 목록만 추출
            List<Long> recommendedHallIds = recommendations.stream()
                    .map(CourseHallDTO::getHallId) // (CourseHallDTO에 getHallId()가 있다고 가정)
                    .toList();
            // (예외처리)
            if (recommendedHallIds.isEmpty()) {
                return List.of(); // Collections.emptyList()
            }
            // '전시관 ID 목록'대로 DB에서 하나씩 호출
            List<CourseItemDTO> finalResponseItems = new ArrayList<>();

            for (Long hallId : recommendedHallIds) {
                // '전시관' 조회
                // 이 쿼리는 'hall_id' 하나에 해당하는 '전시관' 반환
                List<CourseItemDTO> exhibitionsInThisHall = contentMapper.findExhibitionsByHallIdAndCriteria(
                        hallId,
                        mainCategory,
                        grade
                );
                // AI가 추천한 순서대로 리스트에 추가
                finalResponseItems.addAll(exhibitionsInThisHall);
            }
            return finalResponseItems;
        } else {
//            List<CourseItemDTO> candidates = contentMapper.findSimilarSciencePlace(currentId, mainCategory, grade);
            return null;
        }
    }

}