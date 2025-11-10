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
     * [수정] '체험 장소'가 아닌 '장소 내 활동'을 명확히 요청
     */
    private String buildPrompt(Request req, PlaceDetailsForAI details) {

        return String.format(
                "당신은 10년 차 과학 교사이며, 초등학생 눈높이에 맞는 창의적인 체험활동 전문가입니다. " +
                        "다음 정보를 바탕으로, 학생이 '1번 장소' **내에서** 과학 원리를 배울 수 있는 구체적인 **'체험 활동(Activity)'** 3가지를 추천해줘.\n" +
                        "1. 장소 이름: %s\n" +
                        "2. 장소 상세설명: %s\n" +
                        "3. 대상 학년: %d학년\n" +
                        "4. 연계 과학 단원 (주제): %s\n\n" +
                        "요구사항:\n" +
                        "- 활동 제목(title), 구체적인 활동 방법(description), 연계 단원(relatedUnit, 예: '%d학년 - %s')을 포함해줘.\n" +
                        "- **[매우 중요]** 'relatedExhibit' 필드에는 **절대 다른 장소를 추천하지 마.**\n" +
                        "- 'relatedExhibit' 필드에는 '1번 장소' 내에서 그 '활동'을 수행하기 가장 좋은 **'구체적인 스팟' 또는 '위치'**를 작성해줘. (예: '해변 백사장', '공원 잔디밭', '암석이 있는 해안가', '매표소 앞 안내판')\n" +
                        "- '장소 상세설명'(%s)을 참고하여 '활동 수행 위치'를 추천해야 해.\n" +
                        "- 응답은 반드시 아래 JSON 배열 형식만 반환하고, 다른 텍스트는 절대 추가하지 마.\n" +
                        "[\n" +
                        "  {\"title\": \"...\", \"description\": \"...\", \"relatedUnit\": \"...\", \"relatedExhibit\": \"...\"},\n" +
                        "  {\"title\": \"...\", \"description\": \"...\", \"relatedUnit\": \"...\", \"relatedExhibit\": \"...\"}\n" +
                        "]",
                details.getPlaceName(),   // 1. 장소
                details.getDescription(), // 2. 상세 설명
                req.getGrade(),           // 3. 학년
                req.getUnitName(),        // 4. 단원
                req.getGrade(),           // (요구사항) 예시 학년
                req.getUnitName(),        // (요구사항) 예시 단원
                details.getDescription()  // (요구사항) 상세 설명 (맥락 재참조)
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