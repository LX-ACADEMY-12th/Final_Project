package com.example.demo.dto;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class AdminHallDetailDTO {
    private Long hallId;
    private String hallName; // Vue의 'name' 필드와 매칭됨
    private String scienceCenterName;
    private String addressDetail;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String mainImageUrl;
    private LocalDate startDate;
    private LocalDate endDate;
    private String admissionFee;
    private String openingHours;
    private String description;
}