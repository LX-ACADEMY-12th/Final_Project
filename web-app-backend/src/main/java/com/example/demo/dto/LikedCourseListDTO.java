package com.example.demo.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LikedCourseListDTO {
    private Long id; // NUMERIC(28) -> Long
    private String imageUrl;
    private String subject;
    private String grade;
    private String courseName;
    private String address;
    private String type; // '전시' or '답사'

    // 생성자 등 필요에 따라 추가 가능
}