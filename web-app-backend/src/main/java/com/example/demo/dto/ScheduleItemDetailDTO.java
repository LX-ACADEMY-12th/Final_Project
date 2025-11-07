package com.example.demo.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ScheduleItemDetailDTO {
    private Integer sequence;
    private String itemType;
    private Long sourceItemId;
    private String itemName;
    private String addressDetail;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String description;
    private String mainImageUrl;
    private String exhibitionOpeningHours;
    private String placeOpeningHours;
    private BigDecimal exhibitionAdmissionFee;
    private String placeAdmissionFee;
    private String hallName;
    private String scienceCenterName;

    private List<String> mainCategoryNames;
    private List<String> gradeNames;
    private List<String> subCategoryNames;

    // 누락된 필드 추가
    private String sciencePlaceName;

    // 누락된 필드 추가 (다른 배열과 동일하게 List<String> 사용)
    private List<String> exhibitionList;
}