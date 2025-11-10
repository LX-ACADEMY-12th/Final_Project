package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * AI 체험 활동 추천 관련 DTO를 모아두는 컨테이너 클래스
 */
public class ActivityRecommendDTO {

    /**
     * Controller가 Service로 전달할 요청 DTO
     * (frontend의 params와 일치)
     */
    @Data
    @AllArgsConstructor
    public static class Request {
        private Long placeId;
        private Integer grade; // "6" -> 6
        private String unitName; // "물리" (placeMainCategory)
    }

    /**
     * Service가 Controller로 반환할 응답 DTO
     * (frontend의 recommend-card와 일치)
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private String title;
        private String description;
        private String relatedUnit;
        private String relatedExhibit;
    }

    /**
     * [내부용] Mapper가 Service로 전달할, AI 프롬프트 생성을 위한 DTO
     */
    @Data
    public static class PlaceDetailsForAI {
        private String placeName;
        private String description; // 해당 장소에 있는 전시물 이름 목록
    }
}