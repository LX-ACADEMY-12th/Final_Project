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
    private BigDecimal exhibitionAdmissionFee; // DB 타입이 NUMERIC(28)이므로 BigDecimal이 적합
    private String placeAdmissionFee;       // DB 타입이 VARCHAR(100)
    private String hallName;
    private String scienceCenterName;

    private List<String> mainCategoryNames;
    private List<String> gradeNames;
    private List<String> subCategoryNames;

}
