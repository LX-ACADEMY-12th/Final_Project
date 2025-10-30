package com.example.demo.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class UserWishListDTO {

	// 1. wishlist 테이블 정보
    private Long wishlistId;
    private LocalDateTime likedAt; // 찜한 날짜 (w.created_at)

    // 2. 찜한 대상의 공통 정보
    private String targetType;     // 'exhibition' or 'science_place'
    private Long targetId;
    private String name;           // 전시 이름 또는 장소 이름
    private String mainImageUrl;   // 썸네일 이미지 URL
    private Double averageRating;  // 평균 평점
    private Long totalReviews;     // 총 리뷰 수
    
    // 추가 전시 카드의 정보를 불러와야함
    private String type;
    private String location;
    
}
