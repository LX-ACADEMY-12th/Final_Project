package com.example.demo.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.demo.dto.ReviewCreatedDTO;
import com.example.demo.dto.ReviewResponseDTO;

@Mapper
public interface ReviewMapper {

    // ===================== 조회 =====================

	List<ReviewResponseDTO> findReviewsByTarget(Map<String, Object> params);
	
	/**
     * [추가] 카운트 쿼리용 메서드
     */
    long countReviewsByTarget(
            @Param("targetId") Long targetId,
            @Param("targetType") String targetType);

    List<String> findPhotosByReviewId(@Param("reviewId") Long reviewId);
    
    List<String> findAllPhotosByTarget(
    	    @Param("targetType") String targetType, 
    	    @Param("targetId") Long targetId
    	);

    Map<String, Object> findReviewMeta(@Param("reviewId") Long reviewId);

    // ===================== 생성 =====================

    int insertReview(@Param("dto") ReviewCreatedDTO dto,
                     @Param("userId") Long userId);

    int insertReviewPhoto(@Param("reviewId") Long reviewId,
                          @Param("photoUrl") String photoUrl,
                          @Param("sequence") int sequence);
    
    // ===================== 수정 =====================

    int updateReview(@Param("reviewId") Long reviewId,
                     @Param("userId") Long userId,
                     @Param("dto") ReviewCreatedDTO dto);
    
    void deletePhotosByReviewId(Long reviewId);

    // ===================== 삭제 =====================

    int deleteReview(@Param("reviewId") Long reviewId,
                     @Param("userId") Long userId);

    int adminDeleteReview(@Param("reviewId") Long reviewId);

    // ===================== 좋아요 =====================

    int insertReviewLike(@Param("reviewId") Long reviewId,
                         @Param("userId") Long userId);

    int deleteReviewLike(@Param("reviewId") Long reviewId,
                         @Param("userId") Long userId);

    int updateReviewLikeCount(@Param("reviewId") Long reviewId,
                              @Param("amount") int amount);

    int existsReviewLike(@Param("reviewId") Long reviewId,
                         @Param("userId") Long userId);

    // ===================== 신고 =====================

    // 중복이면 PK 예외 발생 -> 서비스에서 DuplicateKeyException으로 변환되어 잡음
    int insertReviewReport(@Param("reviewId") Long reviewId,
                           @Param("reporterUserId") Long reporterUserId);


    // [선택] 임계치 이상이면 한 방에 삭제 (동시성 안전)
    int adminDeleteReviewIfReportedAtLeast(@Param("reviewId") Long reviewId,
                                           @Param("threshold") int threshold);
    

    // ===================== 집계 =====================

    int recomputeTargetStats(@Param("targetId") Long targetId,
                             @Param("targetType") String targetType);
    
    List<Long> findLikedReviewIdsByTargetAndUser(Map<String, Object> params);
    
    // ===================== 사진 썸네일 띄우기 ====================
    List<java.util.Map<String, Object>> findPhotosByReviewIds(
    								@Param("reviewIds") List<Long> reviewIds);
}