package com.example.demo.service;

import com.example.demo.dto.CourseHallDTO;
import com.example.demo.dto.CourseItemDTO; // [!!] DTO만 import
import com.example.demo.mapper.ExhibitionMapper;
import com.example.demo.mapper.ContentMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Slf4j
@Service
public class RecommendService {

    private final ContentMapper contentMapper;
    private final LlmApiService llmApiService; // 범용화된 AI 서비스

    public RecommendService(ContentMapper contentMapper, LlmApiService llmApiService) {
        this.contentMapper = contentMapper;
        this.llmApiService = llmApiService;
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

            // 2. '과학 장소' 추천용 프롬프트 생성 (신규)
            String prompt = buildSciencePlacePrompt(currentId, candidates, mainCategory, subCategories, grade);
            log.info("[RecommendService] AI에게 전송할 최종 프롬프트:\n{}", prompt);

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
    private String buildSciencePlacePrompt(Long currentPlaceId, List<CourseItemDTO> candidates,
                                           String mainCategory, String subCategories, String grade) {
        StringBuilder sb = new StringBuilder();
        sb.append("당신은 과학관의 '야외/특별 장소'를 추천하는 AI 도슨트입니다.\n");
        sb.append("현재 사용자는 ID " + currentPlaceId + " 장소에 있습니다.\n");
        sb.append(String.format("사용자는 '%s', '%s', '%s' 교과 과정에 관심이 있습니다.\n",
                grade, mainCategory, (subCategories != null ? subCategories : "전체")));
        sb.append("아래는 사용자의 관심사와 관련있는 '다음 추천 장소' 후보 목록입니다.\n\n");

        // CourseItemDTO의 필드를 사용하여 프롬프트 구성
        for (CourseItemDTO place : candidates) {
            sb.append(String.format("- ID: %d, 장소 이름: %s\n", place.getPlaceId(), place.getPlaceName()));

            // 버그 수정: '관련 교과 주제'에 'getSubjectName()'을 사용 (getGradeName()이 아님)
            String subject = (place.getSubjectName() != null) ? place.getSubjectName() : "정보 없음";
            sb.append(String.format("- 관련 교과 주제: %s\n", subject));

            sb.append(String.format("- 관련 세부 단원: %s\n", (place.getHashtags() != null) ? String.join(", ", place.getHashtags()) : "정보 없음"));

            String desc = (place.getDescription() != null) ? place.getDescription() : "설명 없음";
            String truncatedDesc = desc.length() > 500 ? desc.substring(0, 500) + "..." : desc;
            sb.append(String.format("  - 장소 설명: %s\n\n", truncatedDesc.trim()));
        }

        sb.append("\n[미션]\n");
        // 수정 1: "2개만" -> "최대 3개만" (모순 제거)
        sb.append("이 후보 '장소' 중에서, 사용자가 다음으로 방문하기에 가장 교육적이고 흥미로운 순서로 **최대 3개**만 골라주세요.\n");
        sb.append("순서를 정할 때는 (1)장소 설명, (2)관련 교과 주제, (3)관련 세부 단원을 중점적으로 고려해야 합니다.\n");

        // 수정 2: 후보가 적을 경우의 가이드라인 명시 (유연성)
        sb.append("후보가 3개 미만(예: 1개 또는 2개)일 경우, 그 후보들 안에서만 순서를 정해 알려주세요.\n");

        // 수정 3: "ID 3개를" -> "ID를" (경직된 요청 제거)
        sb.append("최종 응답은 추천하는 장소의 ID를 쉼표(,)로 구분해서 순서대로 알려주세요.\n");
        sb.append("오직 ID와 쉼표 외에는 아무런 설명도 붙이지 마세요.\n");

        // 수정 4: 예시를 다양하게 제공
        sb.append("예시 (후보가 3개 이상일 때): 102,105,108\n");
        sb.append("예시 (후보가 2개 뿐일 때): 102,105\n");
        sb.append("예시 (후보가 1개 뿐일 때): 102\n");
        sb.append("예시 (추천할 것이 없을 때):"); // (아무것도 출력하지 않음)

        return sb.toString();
    }
}