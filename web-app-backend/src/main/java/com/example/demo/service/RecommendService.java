package com.example.demo.service;

import com.example.demo.dto.CourseItemDTO; // [!!] DTO만 import
import com.example.demo.mapper.ExhibitionMapper;
import com.example.demo.mapper.PlaceMapper;
import org.springframework.stereotype.Service;

import java.util.List;
// [!!] Entity, Collectors import가 필요 없어짐

@Service
public class RecommendService {

    private final PlaceMapper placeMapper;

    private final LlmApiService llmApiService; //

    public RecommendService(ExhibitionMapper exhibitionMapper, PlaceMapper placeMapper, LlmApiService llmApiService) {
        this.placeMapper = placeMapper;
        this.llmApiService = llmApiService;
    }

    public List<CourseItemDTO> getRecommendations(
            String type, Long currentId, String mainCategory, String subCategories, String grade
    ) {

        if ("exhibition".equals(type)) {
            List<CourseItemDTO> candidates = placeMapper.findSimilarExhibition(currentId, mainCategory, grade);
            return llmApiService.getAiRecommendations(currentId, candidates);
        } else {
            List<CourseItemDTO> candidates = placeMapper.findSimilarSciencePlace(currentId, mainCategory, grade);
            return llmApiService.getAiRecommendations(currentId, candidates); // AI 호출 -> ID 3개 값 배열을 리턴
        }
    }

}