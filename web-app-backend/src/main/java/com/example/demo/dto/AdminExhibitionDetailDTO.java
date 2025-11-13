package com.example.demo.dto;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class AdminExhibitionDetailDTO {
    private Long exhibitionId;
    private String exhibitionName; // Vue의 'name' 필드와 매칭됨
    private Long hallId;
    private String addressDetail; // 지오코딩용
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String type;
    private LocalDate startDate;
    private LocalDate endDate;
    private String admissionFee;
    private String openingHours;
    private String mainImageUrl;
    private String description;

    // N:M 매핑 ID
    private List<Long> gradeIds;
    private List<Long> subCategoryIds;
}