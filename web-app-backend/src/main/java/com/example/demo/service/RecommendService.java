package com.example.demo.service;

import com.example.demo.dto.CourseItemDTO; // [!!] DTO만 import
import com.example.demo.mapper.ExhibitionMapper;
import com.example.demo.mapper.PlaceMapper;
import org.springframework.stereotype.Service;

import java.util.List;
// [!!] Entity, Collectors import가 필요 없어짐

@Service
public class RecommendService {

    private final ExhibitionMapper exhibitionMapper;
    private final PlaceMapper placeMapper;

    private final LlmApiService llmApiService; //

    public RecommendService(ExhibitionMapper exhibitionMapper, PlaceMapper placeMapper, LlmApiService llmApiService) {
        this.exhibitionMapper = exhibitionMapper;
        this.placeMapper = placeMapper;
        this.llmApiService = llmApiService;
    }

    public List<CourseItemDTO> getRecommendations(
            String type, Long currentId, String mainCategory, String subCategories, String grade
    ) {

        if ("exhibition".equals(type)) {
            // Mapper가 DTO 목록을 바로 반환 (변환 로직 삭제)
            return exhibitionMapper.findSimilarExhibitions(
                    currentId, mainCategory, grade
            );
        } else {
            List<CourseItemDTO> candidates = placeMapper.findSimilarSciencePlace(currentId, mainCategory, grade);
            return llmApiService.getAiRecommendations(currentId, candidates); // AI 호출 -> ID 3개 값 배열을 리턴
        }
    }

}