package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.ExhibitionDetailDTO;
import com.example.demo.service.ExhibitionDetailService;

@RestController
@RequestMapping("/api/exhibitions") // 공통 url
public class ExhibitionDetailController {

	@Autowired
	private ExhibitionDetailService exhibitionDetailService;
	
	/**
	 * 전시 상세 정보를 ID로 조회하는 API
	 * GET /api/exhibition/id <-- 이 url을 처리
	 * */
	@GetMapping
	public ResponseEntity<ExhibitionDetailDTO> getExhibitionById(@RequestParam Long exhibitionId) {
		// 1. URL에서 받은 ID(101)를 서비스(Service)로 전달
		ExhibitionDetailDTO dto = exhibitionDetailService.getfindExhibitionDetails(exhibitionId);
		
		// 2. 서비스가 DB에서 가져온 DTO를 프론트엔드로 반환 (JSON 형태로)
		if (dto != null) {
            // 데이터가 있으면 200 (OK) 상태와 함께 DTO 반환
            return ResponseEntity.ok(dto);
        } else {
            // 데이터가 없으면 404 (Not Found) 상태 반환
            return ResponseEntity.notFound().build();
        }
	}
}
