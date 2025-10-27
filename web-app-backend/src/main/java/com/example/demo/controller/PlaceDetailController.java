package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.PlaceDetailDTO;
import com.example.demo.service.PlaceDetailService;

@RestController
@RequestMapping("/api/place") // 공통 url
public class PlaceDetailController {

	@Autowired
	private PlaceDetailService placeDetailService;
	
	/**
	 * 장소 상세 정보를 ID로 조회하는 API
	 * GET /api/place?id...
	 * */
	
	@GetMapping
	public ResponseEntity<PlaceDetailDTO> getPlaceById(
			@RequestParam Long placeId,
			@RequestParam(required = false) String mainCategoryTags,
			@RequestParam(required = false) String subCategoryTags,
			@RequestParam(name = "gradeTags", required = false) String gradeTags) {
		
		// 1. URL로 받은 ID를 서비스로 전달
		PlaceDetailDTO dto = placeDetailService.getfindPlaceDetails(placeId, mainCategoryTags, subCategoryTags, gradeTags);
		
		// 2. 서비스가 DB에서 가져온 DTO를 프론트엔드로 반환 (JSON 형태)
		if (dto != null) {
            // 데이터가 있으면 200 (OK) 상태와 함께 DTO 반환
            return ResponseEntity.ok(dto);
        } else {
            // 데이터가 없으면 404 (Not Found) 상태 반환
            return ResponseEntity.notFound().build();
        }
	
	}
}
