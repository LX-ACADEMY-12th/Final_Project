package com.example.demo.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ExhibitionHall {
    private Long id; // hall_id
    private String hallName;
    private String scienceCenterName;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String addressDetail;
    private String mainImageUrl;
    private LocalDate startDate;
    private LocalDate endDate;
    private String admissionFee;
    private String openingHours;
    private String description;
    private BigDecimal averageRating;
}