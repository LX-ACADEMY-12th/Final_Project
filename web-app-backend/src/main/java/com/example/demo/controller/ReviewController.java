package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

            // ⭐️ [추가] 페이지 파라미터 (기본값: 1페이지, 10개씩)
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ){
		
        // ⭐️ [수정] 서비스 호출 및 반환 타입 변경
		PageResponseDTO<ReviewResponseDTO> reviewPage = 
            reviewService.getfindTargetReview(targetId, targetType, page, size);
            
		return ResponseEntity.ok(reviewPage);
	}
	
	// 리뷰 좋아요 상태 조회: 내가 누른 리뷰들의 ID 목록
	@GetMapping("/liked-status")
	public ResponseEntity<?> getLikedStatus(
	        @RequestParam Long targetId,
	        @RequestParam String targetType,
	        @RequestParam Long viewerUserId // 이미 있는 변수 재사용
	) {
	    List<Long> likedIds = reviewService.findLikedReviewIds(targetId, targetType, viewerUserId);
	    return ResponseEntity.ok(java.util.Map.of("likedReviewIds", likedIds));
	}

	
	/**
	 * 리뷰를 생성하는 API
	 */
	@PostMapping
	public ResponseEntity<String> createReview(
			@RequestPart("dto") ReviewCreatedDTO dto,
			@RequestPart(value = "files", required = false) List<MultipartFile> files
			){
		
		Long userId = 1L; // 임시
		System.out.println("--- createReview 컨트롤러 진입 ---");
		System.out.println("DTO: " + dto.toString());
		System.out.println("UserId: " + userId);
		
		reviewService.createReview(dto, userId, files);
		return ResponseEntity.status(HttpStatus.CREATED).body("리뷰가 성공적으로 등록되었습니다.");
	}
	
	/**
	 * 좋아요 추가 api
	 */
	@PostMapping("/{reviewId}/like")
	public ResponseEntity<String> addLike(
			@PathVariable Long reviewId) {
		
		Long userId = 1L; // 임시
		reviewService.addLike(reviewId, userId);
		return ResponseEntity.ok("좋아요를 눌렀습니다.");
	}
	
	/**
	 * 좋아요 삭제 api
	 */
	@DeleteMapping("/{reviewId}/like")
	public ResponseEntity<String> deleteLike(
			@PathVariable Long reviewId) {
		
		Long userId = 1L; // 임시
		reviewService.deleteLike(reviewId, userId);
		return ResponseEntity.ok("좋아요를 취소했습니다.");
	}
	
	/**
	 * 리뷰 수정 api
	 */
	 @PutMapping("/{reviewId}")
     // ⭐️ [수정] @RequestBody -> @RequestPart로 변경 (files 파라미터 추가)
    public ResponseEntity<?> updateReview(
            @PathVariable Long reviewId,
            @RequestPart("dto") ReviewCreatedDTO dto,
            @RequestPart(value = "files", required = false) List<MultipartFile> files
    ) {
        Long userId = 1L; // TODO: 인증 연동
		System.out.println("--- UpdateReview 컨트롤러 진입 ---");
		System.out.println("DTO: " + dto.toString());
		System.out.println("UserId: " + userId);
		
        // ⭐️ [수정] 서비스 호출 시 files 전달
        reviewService.updateReview(reviewId, userId, dto, files);
        return ResponseEntity.ok("수정 성공");
    }
	
	 /**
	  * 리뷰 삭제 api
	  */
	 @DeleteMapping("/{reviewId}")
	 public ResponseEntity<String> deleteReview(
			@PathVariable Long reviewId) {
		
		Long userId = 1L; // 임시
		reviewService.deleteReview(reviewId, userId);
		return ResponseEntity.ok("리뷰 삭제 성공");
	}
	 
	 // 리뷰 신고
	 @PostMapping("/{reviewId}/report")
	 public ResponseEntity<?> reportReview(@PathVariable Long reviewId) {
        Long userId = 1L; // TODO: 인증 연동
        reviewService.insertReviewReport(reviewId, userId);
        return ResponseEntity.ok("리뷰 신고 성공");
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
}