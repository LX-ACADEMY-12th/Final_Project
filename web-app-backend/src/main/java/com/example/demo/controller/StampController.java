package com.example.demo.controller;

import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.dto.StampRequestDTO;
import com.example.demo.dto.StampResponseDTO;
import com.example.demo.service.StampService;

import java.util.List;

@RestController
@RequestMapping("/api/stamps") // API 경로 (예: /api/stamps)
public class StampController {
	
	@Autowired
	private StampService stampService;
	
	/**
     * 스탬프 방문 인증 생성 (GPS)
     * POST /api/stamps
     */
    @PostMapping
    public ResponseEntity<StampResponseDTO> createStamp(
            @RequestBody StampRequestDTO requestDTO) {
        
        // 서비스 호출
        StampResponseDTO responseDTO = stampService.createStamp(requestDTO);
        
        // 인증 성공 시: 200 OK 상태와 인증된 스탬프 정보 반환
        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<StampResponseDTO>> getStampsByUserId(
            @PathVariable Long userId) {
        List<StampResponseDTO> stampList = stampService.getStampsByUserId(userId);
        // 조회된 스탬프 목록과 HTTP 200 OK 상태를 반환
        return ResponseEntity.ok(stampList);
    }

    
    /**
     * [추가] StampService에서 발생한 RuntimeException 처리
     * (예: "거리가 멉니다", "중복 인증", "좌표 없음")
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleStampException(RuntimeException ex) {
        
        // 이제 HttpStatus.BAD_REQUEST에서 에러가 나지 않습니다.
    	return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ex.getMessage());
    }

}
