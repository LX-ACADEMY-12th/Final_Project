package com.example.demo.dto;

import lombok.Data;

import java.util.List;
// 프론트엔드에서 보낸 전체 JSON 페이로드
@Data
public class RecommendedCourseSaveRequestDTO {
    private Long userId;            // 스케줄을 저장하는 사용자의 ID
    private String scheduleName;    // 새 스케줄 이름 (예: "AI 추천: 빛의 마법 특별전 코스")
    private Long sourceId;     // 추천을 유발한 장소/전시의 ID
    private String sourceCourseType; // 소스코스타입 -> 전시 추천? 장소 추천?
    private List<RecommendedCourseItemDTO> items; // 추천 코스에 포함된 아이템 목록
}
