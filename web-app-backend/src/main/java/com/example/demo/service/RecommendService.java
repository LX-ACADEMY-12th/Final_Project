package com.example.demo.service;

import com.example.demo.dto.CourseHallDTO;
import com.example.demo.dto.CourseItemDTO; // [!!] DTO만 import
import com.example.demo.mapper.ContentMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
@Service
public class RecommendService {

    private final ContentMapper contentMapper;
    private final LlmApiService llmApiService; // 범용화된 AI 서비스
    private final WeatherService weatherService;

    public RecommendService(ContentMapper contentMapper, LlmApiService llmApiService, WeatherService weatherService) {
        this.contentMapper = contentMapper;
        this.llmApiService = llmApiService;
        this.weatherService = weatherService;
    }

    public List<CourseItemDTO> getRecommendations(
            String type, Long currentId, String mainCategory, String subCategories, String grade
    ) {

        if ("exhibition".equals(type)) {
            // --- 1. '전시관' 추천 로직 ---

            // 1. AI에게 추천할 '전시관' 후보 조회
            List<CourseHallDTO> candidates = contentMapper.findSimilarExhibition(currentId, mainCategory, grade);
            if (candidates == null || candidates.isEmpty()) {
                return List.of();
            }

            // 2. '전시관' 추천용 프롬프트 생성 (로직 이동)
            String prompt = buildHallPrompt(currentId, candidates, mainCategory, subCategories, grade);

            // 3. AI 호출 (ID 목록만 받음)
            List<Long> recommendedIds = llmApiService.getAiRecommendedIds(prompt);

            // 4. AI 추천 ID 순서대로 '전시관 후보' 리스트 재정렬
            List<CourseHallDTO> recommendations = llmApiService.reorderListByIds(
                    candidates,
                    recommendedIds,
                    CourseHallDTO::getHallId // [!!] ID 추출 람다 전달
            );

            // 5. 추천된 '전시관 ID' 목록으로 실제 '전시물(Item)' 목록 조회
            // (이하 로직은 기존과 동일)
            List<Long> recommendedHallIds = recommendations.stream()
                    .map(CourseHallDTO::getHallId)
                    .toList();
            if (recommendedHallIds.isEmpty()) {
                return List.of();
            }

            List<CourseItemDTO> finalResponseItems = new ArrayList<>();
            for (Long hallId : recommendedHallIds) {
                List<CourseItemDTO> exhibitionsInThisHall = contentMapper.findExhibitionsByHallIdAndCriteria(
                        hallId, mainCategory, grade
                );
                finalResponseItems.addAll(exhibitionsInThisHall);
            }
            return finalResponseItems;

        } else {
            // --- 2. '과학 장소' 추천 로직 ---
            // 1. AI에게 추천할 '과학 장소' 후보 조회
            List<CourseItemDTO> candidates = contentMapper.findSimilarSciencePlace(currentId, mainCategory, grade);

            log.info("[RecommendService] '과학 장소' 후보 {}건 조회 (currentId: {}, mainCategory: {})",
                    (candidates != null ? candidates.size() : 0), currentId, mainCategory);
            log.info("[RecommendService] 조회된 '과학 장소' 후보 목록: {}", candidates);

            if (candidates == null || candidates.isEmpty()) {
                return List.of();
            }

            // ★★★ 각 장소별 날씨 정보 조회 ★★★
            Map<Long, String> placeWeatherMap = new HashMap<>();

            for (CourseItemDTO place : candidates) {
                // 위도/경도가 있는 경우에만 날씨 조회
                if (place.getLatitude() != null && place.getLongitude() != null) {
                    String weather = weatherService.getCurrentWeatherByLatLon(
                            place.getLatitude(),
                            place.getLongitude()
                    );
                    placeWeatherMap.put(place.getPlaceId(), weather);

                    log.info("[RecommendService] 장소 ID={}, 이름={}, 날씨={}",
                            place.getPlaceId(), place.getPlaceName(), weather);
                } else {
                    log.warn("[RecommendService] 장소 ID={}, 이름={} - 위치 정보 없음",
                            place.getPlaceId(), place.getPlaceName());
                    placeWeatherMap.put(place.getPlaceId(), "날씨 정보 없음");
                }
            }

            // 2. 날씨 정보를 포함한 프롬프트 생성
            String prompt = buildSciencePlacePromptWithWeather(
                    currentId, candidates, placeWeatherMap,
                    mainCategory, subCategories, grade
            );
            log.info("[RecommendService] AI에게 전송할 최종 프롬프트:\n{}", prompt);

//            // 2. '과학 장소' 추천용 프롬프트 생성 (신규)
//            String prompt = buildSciencePlacePrompt(currentId, candidates, mainCategory, subCategories, grade);
//            log.info("[RecommendService] AI에게 전송할 최종 프롬프트:\n{}", prompt);

            // 3. AI 호출 (ID 목록만 받음)
            List<Long> recommendedIds = llmApiService.getAiRecommendedIds(prompt);

            // 4. AI 추천 ID 순서대로 '과학 장소 후보' 리스트 재정렬
            // '과학 장소'는 CourseItemDTO를 바로 사용하므로, 이 자체가 최종 응답임
            List<CourseItemDTO> finalResponseItems = new ArrayList<>();
            for (Long placeId : recommendedIds) {
                List<CourseItemDTO> places = contentMapper.findSciencePlaceByPlaceIdAndCriteria(
                        placeId, mainCategory, grade
                );
                finalResponseItems.addAll(places);
            }
            return finalResponseItems;

        }
    }

    /**
     * AI에게 지시할 프롬프트를 만드는 메소드 ('전시관' 추천용)
     */
    private String buildHallPrompt(Long currentHallId, List<CourseHallDTO> candidates,
                                   String mainCategory, String subCategories, String grade) {
        StringBuilder sb = new StringBuilder();
        sb.append("당신은 과학관의 동선을 추천하는 AI 도슨트입니다.\n");
        sb.append("현재 사용자는 ID " + currentHallId + " 전시관에 있습니다.\n");
        // [!!] 프롬프트에 컨텍스트(학년, 과목)를 동적으로 반영
        sb.append(String.format("사용자는 '%s', '%s', '%s' 교과 과정에 관심이 있습니다.\n",
                grade, mainCategory, (subCategories != null ? subCategories : "전체")));
        sb.append("아래는 현재 위치에서 가깝고, 이 관심사에 맞는 '다음 추천 전시관' 후보 목록입니다.\n\n");

        for (CourseHallDTO hall : candidates) {
            sb.append(String.format("- ID: %d, 전시관 이름: %s\n", hall.getHallId(), hall.getHallName()));
            sb.append(String.format("  - 관련 전시 개수: %d개\n", hall.getRelevantExhibitionCount()));
            sb.append(String.format("  - 관련 교과 주제: %s\n",
                    (hall.getSubCategories() != null) ? String.join(", ", hall.getSubCategories()) : "정보 없음"));

            String desc = (hall.getCombinedDescriptions() != null) ? hall.getCombinedDescriptions() : "설명 없음";
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
     * AI에게 지시할 프롬프트를 만드는 메소드 ('과학 장소' 추천용)
     */
    /**
     * AI에게 지시할 프롬프트를 만드는 메소드 ('과학 장소' 추천용 + 각 장소별 날씨 포함)
     */
    private String buildSciencePlacePromptWithWeather(
            Long currentPlaceId,
            List<CourseItemDTO> candidates,
            Map<Long, String> placeWeatherMap,
            String mainCategory,
            String subCategories,
            String grade) {

        StringBuilder sb = new StringBuilder();
        sb.append("당신은 과학관의 '야외/특별 장소'를 추천하는 AI 도슨트입니다.\n");
        sb.append("현재 사용자는 ID " + currentPlaceId + " 장소에 있습니다.\n");
        sb.append(String.format("사용자는 '%s', '%s', '%s' 교과 과정에 관심이 있습니다.\n\n",
                grade, mainCategory, (subCategories != null ? subCategories : "전체")));

        sb.append("아래는 사용자의 관심사와 관련있는 '다음 추천 장소' 후보 목록입니다.\n");
        sb.append("**각 장소의 현재 날씨 정보도 함께 제공되니 반드시 고려하세요.**\n\n");

        // ★★★ 각 장소별 정보 + 해당 위치의 날씨 정보 ★★★
        for (CourseItemDTO place : candidates) {
            sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
            sb.append(String.format("📍 ID: %d | 장소 이름: %s\n", place.getPlaceId(), place.getPlaceName()));

            // 위치 유형 (실내/야외 구분)
            String locationType = "정보없음";
            if (place.getPlaceType() != null) {
                if (place.getPlaceType().contains("야외") ||
                        place.getPlaceType().toLowerCase().contains("outdoor") ||
                        place.getPlaceType().contains("실외")) {
                    locationType = "야외 🌳";
                } else {
                    locationType = "실내 🏢";
                }
            }
            sb.append(String.format("   - 위치 유형: %s\n", locationType));

            // ★ 해당 장소의 날씨 정보
            String weatherInfo = placeWeatherMap.getOrDefault(place.getPlaceId(), "날씨 정보 없음");
            String weatherRecommendation = weatherService.getWeatherRecommendation(weatherInfo);

            sb.append(String.format("   - 🌤️ 현재 날씨: %s\n", weatherInfo));
            sb.append(String.format("   - 💡 날씨 가이드: %s\n", weatherRecommendation));

            // 교과 정보
            String subject = (place.getSubjectName() != null) ? place.getSubjectName() : "정보 없음";
            sb.append(String.format("   - 관련 교과 주제: %s\n", subject));

            sb.append(String.format("   - 관련 세부 단원: %s\n",
                    (place.getHashtags() != null) ? String.join(", ", place.getHashtags()) : "정보 없음"));

            // 장소 설명
            String desc = (place.getDescription() != null) ? place.getDescription() : "설명 없음";
            String truncatedDesc = desc.length() > 500 ? desc.substring(0, 500) + "..." : desc;
            sb.append(String.format("   - 장소 설명: %s\n", truncatedDesc.trim()));
            sb.append("\n");
        }

        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");
        sb.append("========== [미션] ==========\n");
        sb.append("위 후보 '장소' 중에서, 사용자가 다음으로 방문하기에 가장 적합한 순서로 **최대 2개**만 골라주세요.\n\n");

        // ★★★ 날씨를 최우선으로 고려하도록 강조 ★★★
        sb.append("**추천 순서를 정할 때 다음 우선순위로 고려하세요:**\n\n");
        sb.append("🥇 **1순위: 각 장소의 현재 날씨 적합성** (가장 중요!)\n");
        sb.append("   - 각 장소마다 제공된 '현재 날씨'와 '날씨 가이드'를 반드시 확인하세요\n");
        sb.append("   - 야외 장소인데 비/눈이 오면 → 우선순위에서 제외하거나 후순위로\n");
        sb.append("   - 실내 장소는 날씨와 무관하게 안전함\n");
        sb.append("   - 날씨가 좋으면 야외 장소를 적극 추천\n");
        sb.append("   - 매우 덥거나 추운 날씨면 실내를 우선 추천\n\n");

        sb.append("🥈 **2순위: 교육적 가치**\n");
        sb.append("   - 장소 설명의 교육적 가치와 흥미도\n");
        sb.append("   - 관련 교과 주제와의 연관성\n\n");

        sb.append("🥉 **3순위: 교과 연관성**\n");
        sb.append("   - 관련 세부 단원의 다양성\n\n");

        sb.append("**중요 규칙:**\n");
        sb.append("- 각 장소의 날씨가 모두 다를 수 있으므로, 반드시 각각 확인하세요!\n");
        sb.append("- 후보가 3개 미만일 경우, 그 안에서만 순서를 정하세요\n");
        sb.append("- 최종 응답은 추천하는 장소의 ID만 쉼표(,)로 구분하여 작성\n");
        sb.append("- **절대 ID와 쉼표 외에는 아무것도 출력하지 마세요!**\n\n");

        sb.append("**응답 형식 예시:**\n");
        sb.append("- 후보 2개 이상: 102,105\n");
        sb.append("- 후보 1개: 102\n");
        sb.append("- 추천 없음: (공백)\n");

        return sb.toString();
    }
}