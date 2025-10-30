package com.example.demo.dto;
import lombok.Data;
import lombok.NoArgsConstructor;

// 매퍼 상호작용 - 테이블 삽입 및 생성된 ID 반환을 위한 헬퍼 DTO
@Data
@NoArgsConstructor // 필드 초기화를 사용하는 경우 필요
public class AiRecommendedCourseDTO {
    // 이 필드는 삽입 후 MyBatis에 의해 채워집니다.
    private Long aiCourseId;
    private String courseName;
    private Long sourceId;
    private String sourceCourseType;
    // weather_condition 필드는 필요시 추가 가능

    public AiRecommendedCourseDTO(String courseName, Long sourceId, String sourceCourseType) {
        this.courseName = courseName;
        this.sourceId = sourceId;
        this.sourceCourseType = sourceCourseType;
    }
}
