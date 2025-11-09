package com.example.demo.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class Exhibition {

    // DB의 'exhibition_id' (bigint)
    private Long id; // (Mapper.xml에서 'exhibition_id'를 'id'로 매핑)
    
    // DB의 'exhibition_name' (varchar)
    private String exhibitionName;
    
    // DB의 'hall_id' (bigint) - (Foreign Key)
    private Long hallId;
    
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String type; // "PERMANENT"
    private LocalDate startDate;
    private LocalDate endDate;
    private String admissionFee;
    private String openingHours;
    private String mainImageUrl;
    private String description;
}