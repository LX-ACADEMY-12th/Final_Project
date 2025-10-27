package com.example.demo.dto;

// Lombok 어노테이션을 쓰거나, 수동으로 Getter/Constructor를 만드세요.
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CourseItemDTO {
    // AI 한테 넘길 장소의 속성들
    private Long placeId;         // (place_id)
    private String placeName;     // (place_name)
    private String description;   // (description)
    private String placeType;     // (place_type)
    private String subjectName;   // (subject_name)
    private String gradeName;     // (grade_name)

}