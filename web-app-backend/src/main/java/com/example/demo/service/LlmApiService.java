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
     * AI 모델에게 '후보 전시관 리스트'를 보내고 최종 추천을 받는 메소드
     */
    public List<CourseHallDTO> getAiRecommendations(Long currentHallId, List<CourseHallDTO> candidates) {

        // DB에서 추려온 후보가 없으면 AI를 호출할 필요가 없습니다.
        if (candidates == null || candidates.isEmpty()) {
            return List.of();
        }

        // 1. AI에게 보낼 프롬프트(지시서) 생성
        String prompt = buildPrompt(currentHallId, candidates);

        try {
            // 2. Vertex AI Gemini 모델 호출 (동기 방식)
            GenerateContentResponse response = generativeModel.generateContent(prompt);

            // 3. AI의 텍스트 응답 파싱
            String aiResponseText = ResponseHandler.getText(response);
            log.info("AI 응답 수신: {}", aiResponseText);

            // 4. AI가 추천한 ID 목록 파싱 (예: "12, 5, 7")
            List<Long> recommendedIds = parseAiResponse(aiResponseText);

            // 5. AI가 추천한 ID 순서대로 원본 리스트 재정렬
            return reorderList(candidates, recommendedIds);

        } catch (IOException e) {
            log.error("Vertex AI 호출 실패:", e);
            // AI 호출 실패 시, DB에서 가져온 후보군 중 2개를 그대로 반환 (Fallback)
            // (사용자에게 추천이 아예 안 나가는 것보다 좋음)
            return candidates.stream().limit(2).toList();
        }
    }

    /**
     * AI에게 지시할 프롬프트를 만드는 메소드 ('전시관' 추천용)
     */
    // [!!] DTO 타입 변경 및 프롬프트 내용 전면 수정
    private String buildPrompt(Long currentHallId, List<CourseHallDTO> candidates) {
        StringBuilder sb = new StringBuilder();
        sb.append("당신은 과학관의 동선을 추천하는 AI 도슨트입니다.\n");
        sb.append("현재 사용자는 ID " + currentHallId + " 전시관에 있습니다.\n");
        sb.append("사용자는 '초등 3학년', '물리' 교과 과정에 관심이 있습니다.\n");
        sb.append("아래는 현재 위치에서 가깝고, 이 관심사에 맞는 '다음 추천 전시관' 후보 목록입니다.\n\n");

        for (CourseHallDTO hall : candidates) {
            sb.append(String.format("- ID: %d, 전시관 이름: %s\n", hall.getHallId(), hall.getHallName()));
            sb.append(String.format("  - 관련 전시 개수: %d개\n", hall.getRelevantExhibitionCount()));
            sb.append(String.format("  - 관련 교과 주제: %s\n",
                    (hall.getSubCategories() != null) ? String.join(", ", hall.getSubCategories()) : "정보 없음"));

            String desc = (hall.getCombinedDescriptions() != null) ? hall.getCombinedDescriptions() : "설명 없음";
            // [!!] 설명이 너무 길면 AI가 힘들어하므로 요약
            String truncatedDesc = desc.length() > 500 ? desc.substring(0, 500) + "..." : desc;
            sb.append(String.format("  - 관련 전시 설명 요약: %s\n\n", truncatedDesc.trim()));
        }

        sb.append("\n[미션]\n");
        sb.append("이 후보 '전시관' 중에서, 사용자가 다음으로 방문하기에 가장 교육적이고 흥미로운 순서로 2개만 골라주세요.\n");
        sb.append("순서를 정할 때는 (1)관련 전시 설명 요약, (2)관련 전시 개수를 중점적으로 고려해야 합니다.\n");
        sb.append("최종 응답은 추천하는 전시관의 ID 2개를 쉼표(,)로 구분해서 순서대로 알려주세요.");
        sb.append("오직 ID와 쉼표 외에는 아무런 설명도 붙이지 마세요.");
        sb.append("예시: 5,2");

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
     * AI가 추천한 ID 순서대로 원본 후보 '전시관' 리스트를 재정렬
     */
    // DTO 타입 변경 및 Map의 Key 추출 로직 수정
    private List<CourseHallDTO> reorderList(
            List<CourseHallDTO> candidates, List<Long> recommendedIds) {
        if (recommendedIds.isEmpty()) {
            return candidates.stream().limit(2).toList();
        }

        // Map의 Key가 'hallId'가 되도록 수정 (getHallId() 사용)
        Map<Long, CourseHallDTO> candidateMap = candidates.stream()
                .collect(Collectors.toMap(CourseHallDTO::getHallId, item -> item));

        return recommendedIds.stream()
                .map(candidateMap::get)
                .filter(item -> item != null)
                .distinct()
                .toList();
    }
}