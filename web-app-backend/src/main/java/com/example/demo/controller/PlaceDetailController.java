package com.example.demo.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.PlaceDetailDTO;
import com.example.demo.service.PlaceDetailService;

@RestController
@RequestMapping("/api/place") // 공통 url
@Slf4j
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
            Authentication authentication,
			@RequestParam(required = false) String mainCategoryTags,
			@RequestParam(required = false) String subCategoryTags,
			@RequestParam(name = "gradeTags", required = false) String gradeTags) {

        Long userId = getUserIdFromAuthentication(authentication);

        // 1. URL로 받은 ID를 서비스로 전달
		PlaceDetailDTO dto = placeDetailService.getfindPlaceDetails(placeId, userId, mainCategoryTags, subCategoryTags, gradeTags);
		
		// 2. 서비스가 DB에서 가져온 DTO를 프론트엔드로 반환 (JSON 형태)
		if (dto != null) {
            // 데이터가 있으면 200 (OK) 상태와 함께 DTO 반환
            return ResponseEntity.ok(dto);
        } else {
            // 데이터가 없으면 404 (Not Found) 상태 반환
            return ResponseEntity.notFound().build();
        }
	
	}

    // 현재 로그인한 사용자 ID를 추출하는 역할
    private Long getUserIdFromAuthentication(Authentication authentication) {
        if (authentication == null) {
            return null; // 인증 객체가 없으면 (토큰이 없거나 유효하지 않아 필터에서 걸러진 경우) null 반환
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            // JwtTokenProvider가 토큰의 'subject'에 userId를 (String으로) 저장했습니다.
            String userIdStr = ((UserDetails) principal).getUsername();
            try {
                return Long.parseLong(userIdStr);
            } catch (NumberFormatException e) {
                log.error("Authentication principal이 Long 타입으로 변환 불가: {}", userIdStr, e);
                return null;
            }
        } else if (principal instanceof String) {
            // (대체 케이스)
            try {
                return Long.parseLong((String) principal);
            } catch (NumberFormatException e) {
                log.error("Authentication principal이 Long 타입으로 변환 불가: {}", principal, e);
                return null;
            }
        }

        log.warn("알 수 없는 Principal 타입: {}", principal.getClass().getName());
        return null;
    }
}
