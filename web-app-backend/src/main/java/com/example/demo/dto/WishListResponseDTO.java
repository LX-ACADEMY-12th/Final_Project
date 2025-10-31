package com.example.demo.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 찜 목록 조회(GET /api/wishlist/my-list) 응답 시 사용할 DTO
 * (SQL 쿼리의 SELECT 절과 일치)
 */
@Data
public class WishListResponseDTO {

    private Long wishlistId;
    private String targetType;
    private Long targetId;
    private String title;
    private String mainImageUrl;
    private Double averageRating; // (SQL이 Double을 반환하는지 Decimal을 반환하는지 확인 필요)
    private Integer totalReviews; // (Long일 수도 있음)

    // --- 찜할 때 저장된 스냅샷 ---
    private String mainCategory;
    private List<String> subCategories; // (SQL의 ARRAY_AGG 결과)
    private String gradeTag;            // (SQL의 ARRAY_AGG 결과가 단일 값일 경우 String)

    private LocalDateTime likedAt;
    private String type;  // (전시 타입 또는 장소 타입)
    private String place; // (CONCAT_WS 결과)
}