package com.example.demo.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrendDataDTO {
    
    // 시간 정보
    private LocalDate date;
    private String dateLabel;        // "2025-01-15" 또는 "1월 3주차"
    private Integer weekOfYear;
    private Integer month;
    private Integer year;
    
    // 장소 정보 (특정 장소 트렌드 조회 시)
    private Long placeId;
    private String placeName;
    
    // 방문 통계
    private Integer visitCount;
    private Integer uniqueUserCount;
    
    // 누적/평균
    private Integer cumulativeCount;
    private Double movingAverage;     // 7일 이동평균
}
