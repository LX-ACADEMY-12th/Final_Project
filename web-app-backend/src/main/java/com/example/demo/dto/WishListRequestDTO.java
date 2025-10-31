package com.example.demo.dto;

import lombok.Data;

import java.util.List;

@Data
public class WishListRequestDTO {

    // (userId는 SecurityContext에서 가져오므로 제외)

    // "exhibition" 또는 "science_place"
    private String targetType;

    // 전시/장소의 ID
    private Long targetId;

    // --- 찜할 때의 스냅샷 정보 ---

    // 1. 메인 카테고리 (예: "물리")
    private String mainCategory;

    // 2. 학년 (예: "초등 3학년")
    private String gradeTag;
}
