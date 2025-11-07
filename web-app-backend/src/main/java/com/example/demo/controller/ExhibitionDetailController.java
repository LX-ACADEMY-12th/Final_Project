package com.example.demo.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// [!!] 1. 필요한 클래스들을 import 합니다.
import com.example.demo.dto.ExhibitionDTO;          // (목록용 DTO)
import com.example.demo.service.ExhibitionService;      // (목록용 Service)
import com.example.demo.dto.ExhibitionDetailDTO;    // (기존 상세용 DTO)
import com.example.demo.service.ExhibitionDetailService; // (기존 상세용 Service)
import java.util.List;                             // (List import)

@RestController
@RequestMapping("/api/exhibitions")
@Slf4j
public class ExhibitionDetailController {

	@Autowired
	private ExhibitionDetailService exhibitionDetailService; // (기존 상세용 서비스)

	// --- ▼ '목록용 서비스'
	@Autowired
	private ExhibitionService exhibitionService; 

	// --- ▼ [!!] 4. '목록 조회' 메서드를 새로 추가합니다. ▼ ---
	/**
	 * (카테고리별) '전체 목록'을 조회하는 API
	 * - GET /api/exhibitions
	 * - GET /api/exhibitions?category=explore
	 */
	// [중요] 'params = "!exhibitionId"' -> exhibitionId 파라미터가 '없는' 요청만 이 메서드가 받음
	@GetMapping(params = "!exhibitionId") 
	public List<ExhibitionDTO> getAllExhibitions(
			@RequestParam(name = "category", required = false) String categoryParam
	) {
		// 방금 만든 ExhibitionService의 메서드 호출
		return exhibitionService.findAllExhibitions(categoryParam);
	}
	
	// --- ▼ [!!] 5. '상세 조회' 메서드를 수정합니다. ▼ ---
	/**
	 * '상세 정보'를 ID로 조회하는 API
	 * - GET /api/exhibitions?exhibitionId=1
	 */
	// [중요] 'params = "exhibitionId"' -> exhibitionId 파라미터가 '있는' 요청만 이 메서드가 받음
	@GetMapping(params = "exhibitionId")
	public ResponseEntity<ExhibitionDetailDTO> getExhibitionById(
			@RequestParam Long exhibitionId, // (required=true가 기본값)
			@RequestParam(required = false) String mainCategoryTags,
			@RequestParam(required = false) String subCategoryTags,
			@RequestParam(required = false) String gradeTags) {
        log.info("Parameters - 조회하려는 전시관 id: {}, mainCategory: {}, grade: {}",
                exhibitionId, mainCategoryTags, gradeTags);
		ExhibitionDetailDTO dto = exhibitionDetailService.getfindExhibitionDetails(
				exhibitionId,
				mainCategoryTags,
				subCategoryTags,
				gradeTags
				);
		
		if (dto != null) {
			return ResponseEntity.ok(dto);
		} else {
			return ResponseEntity.notFound().build();
	}
}
	}