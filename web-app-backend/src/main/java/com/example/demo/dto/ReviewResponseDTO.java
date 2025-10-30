package com.example.demo.dto;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import lombok.Data;

@Data
public class ReviewResponseDTO {

	private Long reviewId;
	private String targetType;
	private Long targetId;
	private String content;
	private BigDecimal rating;
	private Date createdAt;
	private Date updatedAt;
	
	// --- 2. review_like 테이블 (JOIN + COUNT) ---
    private Long likeCount; // 좋아요 개수

    // --- 3. user 테이블 (JOIN) ---
    private String authorName; // 리뷰 작성자 이름
    private String authorProfileImageUrl; // (선택 사항이지만 추천)
    
    // --- 4. review_photo 테이블 (JOIN) ---
    private List<String> photoUrls;
    
    // --- 5. (현재 로그인한 유저 기준) ---
    private boolean likedByCurrentUser; // 현재 유저가 좋아요 눌렀는지 여부
    
    private Long authorId; 
}