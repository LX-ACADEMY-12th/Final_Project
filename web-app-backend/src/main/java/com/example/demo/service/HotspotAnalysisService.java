package com.example.demo.service;

import com.example.demo.dto.HotspotAnalysisDTO;
import com.example.demo.dto.HotspotAnalysisResponse;
import com.example.demo.dto.HotspotAnalysisResponse.DayOfWeekStats;
import com.example.demo.dto.HotspotAnalysisResponse.MonthlyStats;
import com.example.demo.dto.TrendDataDTO;
import com.example.demo.mapper.HotspotAnalysisMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class HotspotAnalysisService {

    private final HotspotAnalysisMapper hotspotAnalysisMapper;

    private static final String[] DAY_LABELS = {"", "월요일", "화요일", "수요일", "목요일", "금요일", "토요일", "일요일"};
    private static final String[] MONTH_LABELS = {"", "1월", "2월", "3월", "4월", "5월", "6월", "7월", "8월", "9월", "10월", "11월", "12월"};

    /**
     * 종합 핫스팟 분석 데이터 조회
     */
    public HotspotAnalysisResponse getHotspotAnalysis(
            LocalDate startDate,
            LocalDate endDate,
            String analysisType,
            int limit
    ) {
        log.info("핫스팟 분석 시작: {} ~ {}, 유형: {}", startDate, endDate, analysisType);

        // 1. 기본 통계
        Map<String, Object> summary = hotspotAnalysisMapper.selectPeriodSummary(startDate, endDate);
        Integer totalVisits = summary != null ? ((Number) summary.get("totalvisits")).intValue() : 0;
        Integer totalUniqueUsers = summary != null ? ((Number) summary.get("totaluniqueusers")).intValue() : 0;

        // 2. 핫스팟 랭킹
        List<HotspotAnalysisDTO> hotspotRanking = hotspotAnalysisMapper.selectHotspotRanking(startDate, endDate, limit);

        // 3. 급상승 장소 (최근 절반 기간 vs 이전 절반 기간 비교)
        long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate);
        LocalDate midDate = startDate.plusDays(daysBetween / 2);
        List<HotspotAnalysisDTO> risingPlaces = hotspotAnalysisMapper.selectRisingPlaces(
                midDate.plusDays(1), endDate,
                startDate, midDate,
                limit
        );

        // 4. 요일별 분포
        List<DayOfWeekStats> dayOfWeekDistribution = buildDayOfWeekStats(
                hotspotAnalysisMapper.selectDayOfWeekDistribution(startDate, endDate)
        );

        // 5. 월별 분포
        List<MonthlyStats> monthlyDistribution = buildMonthlyStats(
                hotspotAnalysisMapper.selectMonthlyDistribution(startDate, endDate)
        );

        // 6. 전체 트렌드 데이터
        List<TrendDataDTO> trendData = hotspotAnalysisMapper.selectOverallTrendDaily(startDate, endDate);
        
        // 이동평균 계산
        calculateMovingAverage(trendData, 7);

        return HotspotAnalysisResponse.builder()
                .startDate(startDate.format(DateTimeFormatter.ISO_DATE))
                .endDate(endDate.format(DateTimeFormatter.ISO_DATE))
                .analysisType(analysisType)
                .totalVisits(totalVisits)
                .totalUniqueUsers(totalUniqueUsers)
                .hotspotRanking(hotspotRanking)
                .risingPlaces(risingPlaces)
                .dayOfWeekDistribution(dayOfWeekDistribution)
                .monthlyDistribution(monthlyDistribution)
                .trendData(trendData)
                .build();
    }

    /**
     * 특정 장소의 트렌드 데이터 조회
     */
    public List<TrendDataDTO> getPlaceTrend(Long placeId, LocalDate startDate, LocalDate endDate) {
        List<TrendDataDTO> trendData = hotspotAnalysisMapper.selectPlaceTrendDaily(placeId, startDate, endDate);
        calculateMovingAverage(trendData, 7);
        return trendData;
    }

    /**
     * 요일별 통계 변환
     */
    private List<DayOfWeekStats> buildDayOfWeekStats(List<Map<String, Object>> rawData) {
        List<DayOfWeekStats> result = new ArrayList<>();
        
        for (Map<String, Object> row : rawData) {
            int dayOfWeek = ((Number) row.get("dayofweek")).intValue();
            
            DayOfWeekStats stats = DayOfWeekStats.builder()
                    .dayOfWeek(dayOfWeek)
                    .dayLabel(dayOfWeek >= 1 && dayOfWeek <= 7 ? DAY_LABELS[dayOfWeek] : "알수없음")
                    .totalVisits(((Number) row.get("totalvisits")).intValue())
                    .topPlaceId(row.get("topplaceid") != null ? ((Number) row.get("topplaceid")).longValue() : null)
                    .topPlaceName((String) row.get("topplacename"))
                    .topPlaceVisits(row.get("topplacevisits") != null ? ((Number) row.get("topplacevisits")).intValue() : 0)
                    .build();
            
            result.add(stats);
        }
        
        return result;
    }

    /**
     * 월별 통계 변환
     */
    private List<MonthlyStats> buildMonthlyStats(List<Map<String, Object>> rawData) {
        List<MonthlyStats> result = new ArrayList<>();
        
        for (Map<String, Object> row : rawData) {
            int month = ((Number) row.get("month")).intValue();
            int year = ((Number) row.get("year")).intValue();
            
            MonthlyStats stats = MonthlyStats.builder()
                    .month(month)
                    .year(year)
                    .monthLabel(year + "년 " + (month >= 1 && month <= 12 ? MONTH_LABELS[month] : month + "월"))
                    .totalVisits(((Number) row.get("totalvisits")).intValue())
                    .topPlaceId(row.get("topplaceid") != null ? ((Number) row.get("topplaceid")).longValue() : null)
                    .topPlaceName((String) row.get("topplacename"))
                    .topPlaceVisits(row.get("topplacevisits") != null ? ((Number) row.get("topplacevisits")).intValue() : 0)
                    .build();
            
            result.add(stats);
        }
        
        return result;
    }

    /**
     * N일 이동평균 계산
     */
    private void calculateMovingAverage(List<TrendDataDTO> trendData, int windowSize) {
        if (trendData == null || trendData.isEmpty()) return;

        int cumulative = 0;
        for (int i = 0; i < trendData.size(); i++) {
            TrendDataDTO current = trendData.get(i);
            cumulative += current.getVisitCount();
            current.setCumulativeCount(cumulative);

            // 이동평균 계산
            if (i >= windowSize - 1) {
                int sum = 0;
                for (int j = i - windowSize + 1; j <= i; j++) {
                    sum += trendData.get(j).getVisitCount();
                }
                current.setMovingAverage((double) sum / windowSize);
            } else {
                // 윈도우 크기보다 작은 경우 현재까지의 평균
                int sum = 0;
                for (int j = 0; j <= i; j++) {
                    sum += trendData.get(j).getVisitCount();
                }
                current.setMovingAverage((double) sum / (i + 1));
            }
        }
    }
}
