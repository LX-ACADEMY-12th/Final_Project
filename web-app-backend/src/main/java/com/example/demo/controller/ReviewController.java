package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dto.PageResponseDTO;
import com.example.demo.dto.PhotoThumbDTO;
import com.example.demo.dto.ReviewCreatedDTO;
import com.example.demo.dto.ReviewResponseDTO;
import com.example.demo.service.ReviewService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/reviews") // 공통 url
public class ReviewController {

	@Autowired
	private ReviewService reviewService;
	
	/**
	 * 리뷰 상세 정보를 targetType, targetId로 조회하는 API
	 * */
	@GetMapping
	public ResponseEntity<PageResponseDTO<ReviewResponseDTO>> getReviewByTarget(
			@RequestParam Long targetId, 
            @RequestParam String targetType,

            // ⭐️ 페이지 파라미터 (기본값: 1페이지, 10개씩)
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ){
		
        // ⭐️ 서비스 호출 및 반환 타입 변경
		PageResponseDTO<ReviewResponseDTO> reviewPage = 
            reviewService.getfindTargetReview(targetId, targetType, page, size);
            
		return ResponseEntity.ok(reviewPage);
	}
	
	// 리뷰 좋아요 상태 조회: 내가 누른 리뷰들의 ID 목록
	@GetMapping("/liked-status")
	public ResponseEntity<List<Long>> getLikedStatus(
	        Authentication authentication,
	        @RequestParam Long targetId,
	        @RequestParam String targetType
	) {
	    Long userId = getUserIdFromAuthentication(authentication);
	    if (userId == null) return ResponseEntity.status(401).build();

	    List<Long> likedIds = reviewService.findLikedReviewIdsByTargetAndUser(targetType, targetId, userId);
	    return ResponseEntity.ok(likedIds); // 배열로 반환 (프론트는 res.data 사용)
	}

	
	/**
	 * 리뷰를 생성하는 API - 로그인 연동 완료
	 */
	@PostMapping("/upload")
	public ResponseEntity<String> createReview(
			// 🟢 Spring Security가 토큰을 검증하고 주입해주는 인증 객체
	        Authentication authentication,
			@RequestPart("dto") ReviewCreatedDTO dto,
			@RequestPart(value = "files", required = false) List<MultipartFile> files
	){
		// 🟢 인증 객체에서 '진짜' userId를 추출합니다.
		Long userId = getUserIdFromAuthentication(authentication);
		log.info("API CALL: createReview - (Authenticated) userId: {}", userId);
		
		System.out.println("--- createReview 컨트롤러 진입 ---");
		System.out.println("DTO: " + dto.toString());
		
		try {
			reviewService.createReview(dto, userId, files);
			log.info("리뷰 업로드 성공 - userId: {}", userId);
            return ResponseEntity.ok("리뷰 업로드가 성공적으로 저장되었습니다.");
		} catch(Exception e) {
			log.error("추천 코스 저장 실패 - userId: {}", userId, e);
			return ResponseEntity.badRequest().body("리뷰 업로드 중 오류 발생: " + e.getMessage());
		}
	}
	
	/**
	 * 좋아요 추가 api
	 */
	@PostMapping("/{reviewId}/like")
	public ResponseEntity<String> addLike(
			Authentication authentication,
			@PathVariable Long reviewId) {
		
		Long userId = getUserIdFromAuthentication(authentication);
        if (userId == null) {
            return ResponseEntity.status(401).body("유효하지 않은 토큰입니다.");
        }

        log.info("API CALL: addlike - (Authenticated) userId: {}", userId);

        try {
        	reviewService.addLike(reviewId, userId);
            log.info("리뷰 도움돼요 성공 - userId: {}", userId );
            return ResponseEntity.ok("'도움돼요'를 눌렀습니다.");
        } catch (Exception e) {
            log.error("리뷰 도움돼요 실패 - userId: {}", userId, e);
            // 예외 유형에 따라 더 구체적인 오류 메시지 반환 고려
            return ResponseEntity.badRequest().body("리뷰 도움돼요 저장 중 오류 발생: " + e.getMessage());
        }
	}
	
	/**
	 * 좋아요 삭제 api
	 */
	@DeleteMapping("/{reviewId}/unlike")
	public ResponseEntity<String> deleteLike(
			Authentication authentication,
			@PathVariable Long reviewId) {
		
		Long userId = getUserIdFromAuthentication(authentication);
        if (userId == null) {
            return ResponseEntity.status(401).body("유효하지 않은 토큰입니다.");
        }
        
        log.info("API CALL: deleteLike - (Authenticated) userId: {}", userId);

        try {
        	reviewService.deleteLike(reviewId, userId);
            log.info("리뷰 도움돼요 취소 성공 - userId: {}", userId );
            return ResponseEntity.ok("'도움돼요'를 취소했습니다.");
        } catch (Exception e) {
            log.error("리뷰 도움돼요 실패 - userId: {}", userId, e);
            // 예외 유형에 따라 더 구체적인 오류 메시지 반환 고려
            return ResponseEntity.badRequest().body("리뷰 도움돼요 취소 중 오류 발생: " + e.getMessage());
        }
		
	}
	
	/**
	 * 리뷰 수정 api - 완료
	 */
	 @PutMapping("/{reviewId}/upload")
     // ⭐️ [수정] @RequestBody -> @RequestPart로 변경 (files 파라미터 추가)
    public ResponseEntity<?> updateReview(
    		Authentication authentication,
            @PathVariable Long reviewId,
            @RequestPart("dto") ReviewCreatedDTO dto,
            @RequestPart(value = "files", required = false) List<MultipartFile> files
    ) {
		 
		Long userId = getUserIdFromAuthentication(authentication);
		
		if (userId == null) {
            return ResponseEntity.status(401).body("유효하지 않은 토큰입니다.");
        }

        log.info("API CALL: review edit - (Authenticated) userId: {}", userId);
		System.out.println("--- UpdateReview 컨트롤러 진입 ---");
		System.out.println("DTO: " + dto.toString());
		
		try {
			reviewService.updateReview(reviewId, userId, dto, files);
            log.info("추천 코스 저장 성공 - userId: {}", userId );
            return ResponseEntity.ok("리뷰 수정 성공");
        } catch (Exception e) {
            log.error("리뷰 수정 실패 - userId: {}", userId, e);
            // 예외 유형에 따라 더 구체적인 오류 메시지 반환 고려
            return ResponseEntity.badRequest().body("리뷰 수정 중 오류 발생: " + e.getMessage());
        }
    }
	
	 /**
	  * 리뷰 삭제 api
	  */
	 @DeleteMapping("/{reviewId}/delete")
	 public ResponseEntity<String> deleteReview(
			 Authentication authentication,
			@PathVariable Long reviewId) {
		
		Long userId = getUserIdFromAuthentication(authentication);
			
		if (userId == null) {
            return ResponseEntity.status(401).body("유효하지 않은 토큰입니다.");
        }
		
		log.info("API CALL: review delete - (Authenticated) userId: {}", userId);
		
		try {
			reviewService.deleteReview(reviewId, userId);
            log.info("리뷰 삭제 성공 - userId: {}", userId );
            return ResponseEntity.ok("리뷰 삭제 성공");
        } catch (Exception e) {
            log.error("리뷰 삭제 실패 - userId: {}", userId, e);
            // 예외 유형에 따라 더 구체적인 오류 메시지 반환 고려
            return ResponseEntity.badRequest().body("리뷰 삭제 중 오류 발생: " + e.getMessage());
        }
	}
	 
	 // 리뷰 신고
	 @PostMapping("/{reviewId}/report")
	 public ResponseEntity<?> reportReview(@PathVariable Long reviewId, Authentication authentication) {
		 Long userId = getUserIdFromAuthentication(authentication);
			
			if (userId == null) {
	            return ResponseEntity.status(401).body("유효하지 않은 토큰입니다.");
	        }
			
			log.info("API CALL: review report - (Authenticated) userId: {}", userId);
			
			try {
				reviewService.insertReviewReport(reviewId, userId);
	            log.info("리뷰 신고 성공 - userId: {}", userId );
	            return ResponseEntity.ok("리뷰 신고 성공");
	        } catch (Exception e) {
	            log.error("리뷰 신고 실패 - userId: {}", userId, e);
	            // 예외 유형에 따라 더 구체적인 오류 메시지 반환 고려
	            return ResponseEntity.badRequest().body("리뷰 신고 중 오류 발생: " + e.getMessage());
	        }
	 }
	 
	 // 사진 전체 모아보기
	 @GetMapping("/target/{targetType}/{targetId}/photos")
	    public ResponseEntity<List<String>> getAllPhotosByTarget(
	            @PathVariable String targetType,
	            @PathVariable Long targetId) {
	        
	        List<String> photoUrls = reviewService.findAllPhotosByTarget(targetType, targetId);
	        return ResponseEntity.ok(photoUrls);
	 }
	 
	// ⭐️ [추가] ReviewSection.vue 상단 썸네일 3개를 위한 API
	 @GetMapping("/photos-summary")
	 public ResponseEntity<List<PhotoThumbDTO>> getPhotoSummaries(
			 @RequestParam String targetType,
			 @RequestParam Long targetId,
			 @RequestParam(defaultValue = "3") int limit) {
		 
		 // ⭐️ 이 서비스 메서드는 Service/Mapper에서 구현해야 합니다.
		 List<PhotoThumbDTO> thumbnails = reviewService.findPhotoThumbnailsByTarget(targetType, targetId, limit);
		 return ResponseEntity.ok(thumbnails);
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