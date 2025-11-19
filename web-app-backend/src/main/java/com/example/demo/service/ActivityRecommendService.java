package com.example.demo.service; // 패키지 경로는 환경에 맞게 조정하세요

// [수정] DTO import 경로 확인 (사용자가 제공한 경로)
import com.example.demo.dto.ActivityRecommendDTO;
import com.example.demo.dto.ActivityRecommendDTO.PlaceDetailsForAI;
import com.example.demo.dto.ActivityRecommendDTO.Request;
import com.example.demo.dto.ActivityRecommendDTO.Response;
import com.example.demo.mapper.ActivityRecommendMapper; // Mapper 경로 확인

// [수정] 1. LlmApiService에서 사용하던 Vertex AI 관련 클래스 import
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.cloud.vertexai.generativeai.ResponseHandler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException; // [수정] 2. IOException import
import java.util.NoSuchElementException; // [수정] javax.persistence 대신 사용
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityRecommendService {

    private final ActivityRecommendMapper activityRecommendMapper;
    private final ObjectMapper objectMapper; // JSON 파싱용

    // GenerativeModel을 직접 주입받습니다.
    private final GenerativeModel generativeModel;

    /**
     * AI 체험 활동 추천 로직
     */
    public List<Response> getRecommendations(Request reqDto) {
        // 1. DB에서 AI 프롬프트에 필요한 정보 조회 (DTO/Mapper가 description을 가져옴)
        PlaceDetailsForAI placeDetails = activityRecommendMapper.findPlaceDetailsById(reqDto.getPlaceId())
                // (JPA 예외 대신 표준 예외 사용)
                .orElseThrow(() -> new NoSuchElementException("Place not found with id: " + reqDto.getPlaceId()));

        // 2. AI에게 보낼 프롬프트 생성 (수정된 buildPrompt 호출)
        String prompt = buildPrompt(reqDto, placeDetails);
        log.info("[AI Prompt] \n{}", prompt);

        // 3. Vertex AI 직접 호출
        String aiResponseJson;
        try {
            // 3-1. AI 모델 호출
            GenerateContentResponse response = generativeModel.generateContent(prompt);
            // 3-2. 텍스트 응답 추출
            aiResponseJson = ResponseHandler.getText(response);
            log.info("[AI Response] \n{}", aiResponseJson);

        } catch (IOException e) {
            log.error("Vertex AI 호출 실패:", e);
            // AI 호출 실패 시, 3-3. 파싱 단계에서 빈 배열을 반환하도록 유도
            aiResponseJson = "[]";
        }

        // 4. AI 응답 (JSON 문자열) 파싱
        try {
            String cleanedJson = cleanAiResponse(aiResponseJson);
            return objectMapper.readValue(cleanedJson, new TypeReference<List<Response>>() {});

        } catch (JsonProcessingException e) {
            log.error("Failed to parse AI response JSON: {}", aiResponseJson, e);
            throw new RuntimeException("AI 응답을 파싱하는 데 실패했습니다.", e);
        }
    }

    /**
     * AI 프롬프트를 동적으로 생성합니다.
     * [수정] 응답 길이를 제한하고 간결성 강조
     */
    private String buildPrompt(Request req, PlaceDetailsForAI details) {

        return String.format(
                "당신은 초등학생 대상 과학 체험활동 전문가입니다. " +
                        "다음 정보를 바탕으로 '%s' **내에서** 할 수 있는 체험 활동 3가지를 추천해주세요.\n\n" +
                        "📍 장소 정보:\n" +
                        "- 장소: %s\n" +
                        "- 설명: %s\n" +
                        "- 학년: %d학년\n" +
                        "- 단원: %s\n\n" +
                        "📋 응답 형식:\n" +
                        "- title: 15자 이내 간결한 제목\n" +
                        "- description: **핵심만 2-3문장**(최대 80자), 초등학생이 바로 실행 가능한 구체적 방법\n" +
                        "- relatedUnit: '%d학년 - %s'\n" +
                        "- relatedExhibit: 장소 내 구체적 위치 (10자 이내, 예: '입구 암석', '2층 전시관')\n\n" +
                        "⚠️ 주의사항:\n" +
                        "- description은 불필요한 설명 없이 '무엇을', '어떻게' 할지만 간결하게\n" +
                        "- relatedExhibit은 반드시 '%s' 내부 위치만 (다른 장소 절대 금지)\n" +
                        "- JSON 배열만 반환 (다른 텍스트 금지)\n\n" +
                        "[\n" +
                        "  {\"title\": \"...\", \"description\": \"...\", \"relatedUnit\": \"...\", \"relatedExhibit\": \"...\"},\n" +
                        "  {\"title\": \"...\", \"description\": \"...\", \"relatedUnit\": \"...\", \"relatedExhibit\": \"...\"}\n" +
                        "]",
                details.getPlaceName(),   // 제목에 장소명
                details.getPlaceName(),   // 1. 장소
                details.getDescription(), // 2. 상세 설명
                req.getGrade(),           // 3. 학년
                req.getUnitName(),        // 4. 단원
                req.getGrade(),           // relatedUnit 예시
                req.getUnitName(),        // relatedUnit 예시
                details.getPlaceName()    // 주의사항 - 장소명 재강조
        );
    }

    /**
     * AI가 응답에 ```json ... ``` 같은 마크다운을 포함할 경우 제거합니다.
     */
    private String cleanAiResponse(String json) {
        if (json == null || json.isBlank()) return "[]";

        int jsonStart = json.indexOf("[");
        int jsonEnd = json.lastIndexOf("]");

        if (jsonStart != -1 && jsonEnd != -1 && jsonEnd > jsonStart) {
            return json.substring(jsonStart, jsonEnd + 1);
        }

        log.warn("응답에서 유효한 JSON 배열을 찾지 못했습니다: {}", json);
        return "[]";
    }
}