package com.example.demo.controller;

import com.example.demo.service.UserPathAnalysisService;
import com.example.demo.dto.UserPathSegmentDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin/stamps")
@RequiredArgsConstructor
public class AdminPathAnalysisController {

    private final UserPathAnalysisService userPathAnalysisService;

    // API 엔드포인트: /api/admin/stamps/path-analysis?startDate=...&endDate=...
    @GetMapping("/path-analysis")
    public ResponseEntity<List<UserPathSegmentDTO>> getPathAnalysisData(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "PLACE") String type
    ) {
        if (startDate.isAfter(endDate)) {
            return ResponseEntity.badRequest().build();
        }

        List<UserPathSegmentDTO> data = userPathAnalysisService.analyzePathSegments(startDate, endDate, type);
        return ResponseEntity.ok(data);
    }
}