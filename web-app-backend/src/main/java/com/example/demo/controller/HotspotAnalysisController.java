package com.example.demo.controller;

import com.example.demo.dto.HotspotAnalysisResponse;
import com.example.demo.dto.TrendDataDTO;
import com.example.demo.service.HotspotAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/admin/hotspot")
@RequiredArgsConstructor
public class HotspotAnalysisController {

    private final HotspotAnalysisService hotspotAnalysisService;

    /**
     * 종합 핫스팟 분석 조회
     * GET /api/admin/hotspot/analysis?startDate=2025-01-01&endDate=2025-01-31&type=DAILY&limit=10
     */
    @GetMapping("/analysis")
    public ResponseEntity<HotspotAnalysisResponse> getHotspotAnalysis(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "DAILY") String type,
            @RequestParam(defaultValue = "10") int limit
    ) {
        log.info("핫스팟 분석 요청: {} ~ {}, 유형: {}, limit: {}", startDate, endDate, type, limit);

        if (startDate.isAfter(endDate)) {
            return ResponseEntity.badRequest().build();
        }

        HotspotAnalysisResponse response = hotspotAnalysisService.getHotspotAnalysis(
                startDate, endDate, type, limit
        );

        return ResponseEntity.ok(response);
    }

    /**
     * 특정 장소의 트렌드 데이터 조회
     * GET /api/admin/hotspot/trend/{placeId}?startDate=2025-01-01&endDate=2025-01-31
     */
    @GetMapping("/trend/{placeId}")
    public ResponseEntity<List<TrendDataDTO>> getPlaceTrend(
            @PathVariable Long placeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        log.info("장소 트렌드 요청: placeId={}, {} ~ {}", placeId, startDate, endDate);

        if (startDate.isAfter(endDate)) {
            return ResponseEntity.badRequest().build();
        }

        List<TrendDataDTO> trendData = hotspotAnalysisService.getPlaceTrend(placeId, startDate, endDate);

        return ResponseEntity.ok(trendData);
    }
}
