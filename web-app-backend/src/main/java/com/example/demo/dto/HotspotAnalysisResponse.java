package com.example.demo.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HotspotAnalysisResponse {
    
    // 분석 메타 정보
    private String startDate;
    private String endDate;
    private String analysisType;      // DAILY, WEEKLY, MONTHLY, DAY_OF_WEEK
    private Integer totalVisits;
    private Integer totalUniqueUsers;
    
    // 핫스팟 랭킹 (상위 N개)
    private List<HotspotAnalysisDTO> hotspotRanking;
    
    // 급상승 장소 (성장률 기준)
    private List<HotspotAnalysisDTO> risingPlaces;
    
    // 요일별 분포 (DAY_OF_WEEK 분석 시)
    private List<DayOfWeekStats> dayOfWeekDistribution;
    
    // 월별 분포 (MONTHLY 분석 시)
    private List<MonthlyStats> monthlyDistribution;
    
    // 시계열 데이터 (특정 장소 선택 시)
    private List<TrendDataDTO> trendData;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DayOfWeekStats {
        private Integer dayOfWeek;    // 1=월, 7=일
        private String dayLabel;      // "월요일"
        private Integer totalVisits;
        private Long topPlaceId;
        private String topPlaceName;
        private Integer topPlaceVisits;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MonthlyStats {
        private Integer month;
        private Integer year;
        private String monthLabel;    // "2025년 1월"
        private Integer totalVisits;
        private Long topPlaceId;
        private String topPlaceName;
        private Integer topPlaceVisits;
    }
}
