package com.example.demo.dto;

import lombok.Data;

import java.util.List;

// 프론트엔드에서 보낸 추천 코스목록의 개별 아이템을 나타냄.
@Data
public class RecommendedCourseItemDTO {
    // 프론트엔드의 'id' (place_id 또는 exhibition_id에 해당)
    private Long exhibitionId;
    private Long placeId;

    // 프론트엔드의 'number' (순서)
    private Integer sequence;

    private String itemType;

    // --- 스냅샷 필드 ---
    private String categoryName;
    private String gradeName;
    private List<String> subCategories;
}