package com.example.demo.dto;

import lombok.Data;

@Data
public class FinalScheduleDTO {
    // 이 필드는 삽입 후 MyBatis에 의해 채워집니다.
    private Long scheduleId;
    private Long userId;
    private String scheduleName;
    private String sourceCourseType;
    private Long sourceAiCourseId; // 저장된 AI 추천 기록과의 연결고리

    public FinalScheduleDTO(Long userId, String scheduleName, String sourceCourseType, Long sourceAiCourseId) {
        this.userId = userId;
        this.scheduleName = scheduleName;
        this.sourceCourseType = sourceCourseType;
        this.sourceAiCourseId = sourceAiCourseId;
    }
}
