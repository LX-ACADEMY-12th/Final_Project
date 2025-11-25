package com.example.demo.mapper;

import com.example.demo.dto.HotspotAnalysisDTO;
import com.example.demo.dto.TrendDataDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Mapper
public interface HotspotAnalysisMapper {

    /**
     * 기간 내 장소별 핫스팟 랭킹 조회
     */
    List<HotspotAnalysisDTO> selectHotspotRanking(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("limit") int limit
    );

    /**
     * 요일별 방문 분포 조회
     */
    List<Map<String, Object>> selectDayOfWeekDistribution(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * 월별 방문 분포 조회
     */
    List<Map<String, Object>> selectMonthlyDistribution(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * 급상승 장소 조회 (최근 기간 vs 이전 기간 비교)
     */
    List<HotspotAnalysisDTO> selectRisingPlaces(
            @Param("recentStart") LocalDate recentStart,
            @Param("recentEnd") LocalDate recentEnd,
            @Param("previousStart") LocalDate previousStart,
            @Param("previousEnd") LocalDate previousEnd,
            @Param("limit") int limit
    );

    /**
     * 특정 장소의 일별 트렌드 데이터 조회
     */
    List<TrendDataDTO> selectPlaceTrendDaily(
            @Param("placeId") Long placeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * 전체 일별 트렌드 데이터 조회
     */
    List<TrendDataDTO> selectOverallTrendDaily(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * 기간 내 총 방문 통계 조회
     */
    Map<String, Object> selectPeriodSummary(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
