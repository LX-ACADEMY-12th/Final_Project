package com.example.demo.dto;

import lombok.Data;
// 프론트엔드에서 보낸 추천 코스목록의 개별 아이템을 나타냄.
@Data
public class RecommendedCourseItemDTO {
    // 프론트엔드의 'id' (place_id 또는 exhibition_id에 해당)
    private Long exhibitionId;
    private Long placeId;
    // 프론트엔드의 'number' (순서)
    private Integer sequence;
    // 프론트엔드의 'type' ('science_place' 또는 'exhibition')
    // 스키마 기반으로 ai_course 컨텍스트에서는 'science_place'로 가정
    private String itemType; // 프론트엔드에서 받아서 설정
    // targetId는 ai_course_item의 place_id로 매핑될 예정
}