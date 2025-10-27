package com.example.demo.service;

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
import java.util.stream.Collectors;

@Service
public class LlmApiService {

    private static final Logger log = LoggerFactory.getLogger(LlmApiService.class);

    // [!!] 1. RestTemplate 대신 GenerativeModel 주입
    private final GenerativeModel generativeModel;

    public LlmApiService(GenerativeModel generativeModel) {
        this.generativeModel = generativeModel;
    }

    /**
     * AI 모델에게 후보 리스트를 보내고 최종 추천을 받는 메소드
     */
    public List<CourseItemDTO> getAiRecommendations(Long currentId, List<CourseItemDTO> candidates) {

        // DB에서 추려온 후보가 없으면 AI를 호출할 필요가 없습니다.
        if (candidates == null || candidates.isEmpty()) {
            return List.of();
        }

        // 1. AI에게 보낼 프롬프트(지시서) 생성
        String prompt = buildPrompt(currentId, candidates);

        try {
            // [!!] 2. Vertex AI Gemini 모델 호출 (동기 방식)
            GenerateContentResponse response = generativeModel.generateContent(prompt);

            // [!!] 3. AI의 텍스트 응답 파싱
            String aiResponseText = ResponseHandler.getText(response);
            log.info("AI 응답 수신: {}", aiResponseText);

            // 4. AI가 추천한 ID 목록 파싱 (예: "12, 5, 7")
            List<Long> recommendedIds = parseAiResponse(aiResponseText);

            // 5. AI가 추천한 ID 순서대로 원본 리스트 재정렬
            return reorderList(candidates, recommendedIds);

        } catch (IOException e) {
            log.error("Vertex AI 호출 실패:", e);
            // [!!] AI 호출 실패 시, DB에서 가져온 후보군 3개를 그대로 반환 (Fallback)
            // (사용자에게 추천이 아예 안 나가는 것보다 좋음)
            return candidates.stream().limit(3).toList();
        }
    }

    /**
     * AI에게 지시할 프롬프트를 만드는 메소드
     */
    private String buildPrompt(Long currentId, List<CourseItemDTO> candidates) {
        StringBuilder sb = new StringBuilder();
        sb.append("당신은 과학 교육 코스 추천 전문가입니다.\n");
        sb.append("현재 사용자는 ID " + currentId + " 장소에 있습니다.\n");
        sb.append("아래는 사용자가 좋아할 만한 '후보 장소' 목록입니다. (최대 10개)\n\n");

        for (CourseItemDTO item : candidates) {
            // [!!] 평점(rating) 대신 설명(description)과 타입(placeType)을 제공
            String subject = item.getSubjectName() != null ? item.getSubjectName() : "정보 없음";
            String grade = item.getGradeName() != null ? item.getGradeName() : "정보 없음";
            String placeType = item.getPlaceType() != null ? item.getPlaceType() : "정보 없음";
            String description = item.getDescription() != null ? item.getDescription() : "설명 없음";

            sb.append(String.format("- ID: %d, 이름: %s\n", item.getPlaceId(), item.getPlaceName()));
            sb.append(String.format("  - 주제: %s, 학년: %s, 장소 유형: %s\n", subject, grade, placeType));
            sb.append(String.format("  - 설명: %s\n\n", description.trim())); // AI가 잘 읽도록 설명은 따로
        }

        sb.append("\n이 후보 중에서 현재 장소와 가장 잘 어울리는 코스가 되도록 3개만 골라, 그 ID를 쉼표(,)로 구분해서 순서대로 알려주세요.");
        sb.append("설명과 유형을 보고 가장 교육적이고 흥미로운 코스가 되도록 순서를 정해주세요.");
        sb.append("오직 ID와 쉼표 외에는 아무런 설명도 붙이지 마세요.");
        sb.append("예시: 12,5,7");

        log.info("--- AI 프롬프트 생성 ---");
        log.info(sb.toString());
        log.info("------------------------");

        return sb.toString();
    }

    /**
     * AI 응답 텍스트(예: "12, 5, 7")를 파싱하여 Long 리스트로 변환
     */
    private List<Long> parseAiResponse(String aiResponseText) {
        if (aiResponseText == null || aiResponseText.isBlank()) {
            return List.of();
        }

        try {
            // "12, 5, 7" -> ["12", " 5", " 7"] -> [12L, 5L, 7L]
            return Arrays.stream(aiResponseText.split(","))
                    .map(String::trim)      // " 5 " -> "5"
                    .filter(s -> !s.isEmpty()) // 빈 문자열 제거
                    .map(Long::parseLong)   // "5" -> 5L
                    .toList();
        } catch (NumberFormatException e) {
            log.error("AI 응답(ID) 파싱 실패: {}", aiResponseText, e);
            return List.of(); // 파싱 실패 시 빈 리스트 반환
        }
    }

    /**
     * AI가 추천한 ID 순서대로 원본 후보 리스트를 재정렬
     */
    private List<CourseItemDTO> reorderList(List<CourseItemDTO> candidates, List<Long> recommendedIds) {
        if (recommendedIds.isEmpty()) {
            // AI가 추천 ID를 반환하지 못하면, 그냥 DB 순서(평점순)대로 3개 반환
            return candidates.stream().limit(3).toList();
        }

        // 원본 후보 리스트를 Map<ID, DTO>로 변환 (빠른 조회용)
        Map<Long, CourseItemDTO> candidateMap = candidates.stream()
                .collect(Collectors.toMap(CourseItemDTO::getPlaceId, item -> item));

        // .distinct()를 추가하여 AI가 중복 ID를 반환해도 고유한 리스트만 생성
        return recommendedIds.stream()
                .map(candidateMap::get)
                .filter(item -> item != null)
                .distinct() // [!!] 중복 제거
                .toList();
    }
}