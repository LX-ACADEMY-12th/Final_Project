package com.example.demo.controller;

import com.example.demo.dto.HallDetailDTO;
import com.example.demo.dto.HallDetailResultDTO;
import com.example.demo.dto.HallSimpleDTO;
import com.example.demo.dto.HallSimpleResultDTO;
import com.example.demo.service.ExhibitionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api") // API 공통 경로
public class ExhibitionController {

    private final ExhibitionService exhibitionService;

    /**
     * API 1: 전시관 상세 정보 조회 (LocationSection용)
     * GET /api/halls/{hallId}
     */
    @GetMapping("/halls/{hallId}")
    public ResponseEntity<HallDetailDTO> getHallById(@PathVariable Long hallId) {
        HallDetailDTO hallDetail = exhibitionService.getHallDetail(hallId);
        return ResponseEntity.ok(hallDetail);
    }

    /**
     * API 2: 특정 과학관의 모든 전시관 목록 조회 (ExhibitionHall용)
     * GET /api/centers/{scienceCenterName}/halls
     */
    @GetMapping("/halls")
    public ResponseEntity<List<HallSimpleDTO>> getHallsByScienceCenter(
            @RequestParam String scienceCenterName) {
        log.info("API 2번 /api/halls?scienceCenterName={} 호출 들어옴", scienceCenterName);
        List<HallSimpleDTO> halls = exhibitionService.getHallsByScienceCenterName(scienceCenterName);
        return ResponseEntity.ok(halls);
    }

}