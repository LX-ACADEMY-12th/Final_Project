package com.example.demo.dto;

import lombok.Data;

@Data
public class ContentSearchDTO {
    private String searchType; // "all", "radius", "region"
    private String itemType;   // "전시", "답사"
    private String subject;    // "물리", "화학", ...
    private String grade;      // "초등 3학년", ...
    private Double lat;        // 위도 (radius 검색 시)
    private Double lng;        // 경도 (radius 검색 시)
    private Integer radius;    // 반경 km (radius 검색 시)
    private String region;     // 지역명 (region 검색 시)
}
