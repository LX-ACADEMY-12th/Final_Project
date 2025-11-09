package com.example.demo.dto;

import lombok.Data;
import java.util.List;

@Data
public class PlaceResultDTO {
    private Long id;            // e.exhibition_id as id
    private String title;       // e.exhibition_name as title
    private String subject;     // cmc.category_name as subject
    private String grade;       // MIN(gc.grade_name) as grade
    private String place;       // eh.science_center_name || ' ' || eh.hall_name as place
    private String imageUrl;    // e.main_image_url as imageUrl
    private List<String> hashtags; // ARRAY_AGG(DISTINCT csc.sub_category_name) as hashtags
    private Double lat;         // e.latitude as lat
    private Double lng;         // e.longitude as lng
    private String type;        // e.type as type <-- 추가됨
    // city, district 필드는 쿼리에서 직접 선택하지 않으므로 제거
}
