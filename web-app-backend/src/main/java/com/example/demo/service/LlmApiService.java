package com.example.demo.service;

import com.example.demo.dto.CourseHallDTO;
import com.example.demo.dto.CourseItemDTO;
// Vertex AI / Gemini 관련 클래스 import
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.cloud.vertexai.generativeai.ResponseHandler;
// 로깅 및 유틸리티 import
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class LlmApiService {

    private static final Logger log = LoggerFactory.getLogger(LlmApiService.class);

    // 1. RestTemplate 대신 GenerativeModel 주입
    private final GenerativeModel generativeModel;

    public LlmApiService(GenerativeModel generativeModel) {
        this.generativeModel = generativeModel;
    }

    /**
     * AI 모델에게 '프롬프트'를 보내고 추천 'ID 목록'을 받는 핵심 메소드
     */
    public List<Long> getAiRecommendedIds(String prompt) {
        // 후보가 없는 경우의 처리는 RecommendService가 담당 (프롬프트가 비어있지 않다고 가정)
        try {
            // 1. Vertex AI Gemini 모델 호출
            GenerateContentResponse response = generativeModel.generateContent(prompt);

            // 2. AI의 텍스트 응답 파싱
            String aiResponseText = ResponseHandler.getText(response);
            log.info("AI 응답 수신: {}", aiResponseText);

            // 3. AI가 추천한 ID 목록 파싱 (예: "12, 5, 7")
            return parseAiResponse(aiResponseText);

        } catch (IOException e) {
            log.error("Vertex AI 호출 실패:", e);
            // AI 호출 실패 시, 빈 리스트 반환 (Fallback 처리는 RecommendService가 담당)
            return List.of();
        }
    }

    /**
     * AI 응답 텍스트(예: "12, 5, 7")를 파싱하여 Long 리스트로 변환
     */
    private List<Long> parseAiResponse(String aiResponseText) {
        if (aiResponseText == null || aiResponseText.isBlank()) {
            return List.of();
        }
        try {
            return Arrays.stream(aiResponseText.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty() &&
                            !s.equalsIgnoreCase("null") && s.matches("\\d+"))
                    .map(Long::parseLong)
                    .toList();
        } catch (NumberFormatException e) {
            log.error("AI 응답(ID) 파싱 실패: {}", aiResponseText, e);
            return List.of();
        }
    }

    /**
     * AI가 추천한 ID 순서대로 원본 리스트를 재정렬하는 '범용(Generic)' 메소드
     *
     * @param <T>           재정렬할 객체 타입 (예: CourseHallDTO, CourseItemDTO)
     * @param candidates    원본 후보 리스트
     * @param recommendedIds AI가 추천한 ID 순서
     * @param idExtractor   후보 객체에서 ID(Long)를 추출하는 람다 함수
     * @return 재정렬된 리스트
     */
    public <T> List<T> reorderListByIds(
            List<T> candidates, List<Long> recommendedIds, Function<T, Long> idExtractor) {

        // AI가 추천을 못했거나, 파싱에 실패한 경우 Fallback
        if (recommendedIds.isEmpty()) {
            // 원본 후보 중 2개만 반환 (기존 로직 유지)
            return candidates.stream().limit(2).toList();
        }

        // 원본 후보 리스트를 Map으로 변환 (검색 속도 향상)
        Map<Long, T> candidateMap = candidates.stream()
                .collect(Collectors.toMap(idExtractor, item -> item, (first, second) -> first)); // [!!] 중복 키의 경우 첫 번째 항목 사용

        // AI가 추천한 ID 순서대로 Map에서 객체를 찾아 새 리스트 생성
        return recommendedIds.stream()
                .map(candidateMap::get) // ID에 해당하는 객체 찾기
                .filter(item -> item != null) // Map에 없는 ID(잘못된 ID)가 온 경우 필터링
                .distinct() // 혹시 모를 중복 ID 추천 제거
                .toList();
    }
}