package com.example.demo.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HotspotAnalysisDTO {
    
    // 장소 정보
    private Long placeId;
    private String placeName;
    private Double latitude;
    private Double longitude;
    
    // 방문 통계
    private Integer visitCount;          // 총 방문 수
    private Integer uniqueUserCount;     // 고유 방문자 수
    
    // 핫스팟 점수 (방문수 + 고유방문자 가중치)
    private Double hotspotScore;
    
    // 트렌드 정보
    private Double growthRate;           // 성장률 (%)
    private String trendStatus;          // RISING, STABLE, DECLINING
    
    // 시간 정보 (요일별/월별 분석용)
    private Integer dayOfWeek;           // 1=월, 7=일
    private Integer month;
    private String periodLabel;          // "월요일", "1월" 등
}
