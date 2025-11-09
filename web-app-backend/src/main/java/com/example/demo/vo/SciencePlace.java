package com.example.demo.vo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class SciencePlace {
    private Long id; // place_id
    private String placeName;
    private String description;
    private String mainImageUrl;
    private String addressDetail;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String admissionFee;
    private String openingHours;
}