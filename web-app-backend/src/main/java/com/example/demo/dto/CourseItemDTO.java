package com.example.demo.dto;

// Lombok 어노테이션을 쓰거나, 수동으로 Getter/Constructor를 만드세요.
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CourseItemDTO {
    // AI + UI 공통
    private Long placeId;         // (place_id)
    private String placeName;     // (place_name)
    private String subjectName;   // (subject_name)
    private String gradeName;     // (grade_name)

    // AI가 주로 사용
    private String description;   // (description)
    private String placeType;     // (place_type)

    // UI가 주로 사용
    private String imageUrl;
    private String address;
    private Double latitude;
    private Double longitude;
    private List<String> hashtags;

}