package com.example.demo.dto;

import lombok.Data;
import java.util.List;

@Data
public class PlaceResultDTO {
    private Long id; // 또는 exhibitionId / placeId
    private String title;
    private String subject;
    private String grade; // "3학년", "4학년" ... (서버에서 '초등 ' 제거)
    private String city;
    private String district;
    private String place; // 장소명 (과학관 이름 등)
    private String imageUrl;
    private List<String> hashtags;
    private Double lat;
    private Double lng;
    // 필요에 따라 type('상설'/'기획'), distance(거리) 등 추가 가능
}
