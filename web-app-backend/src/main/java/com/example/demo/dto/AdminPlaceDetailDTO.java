package com.example.demo.dto;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class AdminPlaceDetailDTO {
    private Long placeId;
    private String placeName; // Vue의 'name' 필드와 매칭됨
    private String description;
    private String mainImageUrl;
    private String addressDetail;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String admissionFee;
    private String openingHours;
    private String placeType;

    // N:M 매핑 ID
    private List<Long> gradeIds;
    private List<Long> subCategoryIds;
}