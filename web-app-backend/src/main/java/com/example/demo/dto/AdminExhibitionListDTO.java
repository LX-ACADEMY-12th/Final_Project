package com.example.demo.dto;
import lombok.Data;

@Data
public class AdminExhibitionListDTO {
    private Long id; // exhibition_id
    private String title; // exhibition_name
    private String imageUrl; // main_image_url
    private String type;
    private String subject; // N:M (과학영역)
    private String grade; // N:M (학년)
    private String place; // 소속 전시관 이름
}