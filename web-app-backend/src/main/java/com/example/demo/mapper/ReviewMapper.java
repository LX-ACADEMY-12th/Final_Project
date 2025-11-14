package com.example.demo.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.demo.dto.PhotoThumbDTO;
import com.example.demo.dto.ReviewCreatedDTO;
import com.example.demo.dto.ReviewResponseDTO;
import com.example.demo.dto.ReportedReviewDTO;

@Mapper
public interface ReviewMapper {

    // ===== 목록/카운트 =====
    List<ReviewResponseDTO> findReviewsByTarget(Map<String, Object> params);
    long countReviewsByTarget(@Param("targetId") Long targetId, @Param("targetType") String targetType);

    // ===== 사진 조회 =====
    List<String> findPhotosByReviewId(@Param("reviewId") Long reviewId); // blobName 리스트
    List<Map<String, Object>> findPhotosByReviewIds(@Param("reviewIds") List<Long> reviewIds); // review_id, photo_url
    List<String> findAllPhotosByTarget(@Param("targetType") String targetType, @Param("targetId") Long targetId);
    List<PhotoThumbDTO> findPhotoThumbnailsByTarget(@Param("targetType") String targetType,
                                                    @Param("targetId") Long targetId,
                                                    @Param("limit") int limit);

    // ===== 생성 =====
    int insertReview(@Param("dto") ReviewCreatedDTO dto, @Param("userId") Long userId);
    int insertReviewPhoto(@Param("reviewId") Long reviewId,
                          @Param("photoUrl") String photoUrl,
                          @Param("sequence") Integer sequence);

    // ===== 좋아요 =====
    int existsReviewLike(@Param("reviewId") Long reviewId, @Param("userId") Long userId);
    int deleteReviewLike(@Param("reviewId") Long reviewId, @Param("userId") Long userId);
    int insertReviewLike(@Param("reviewId") Long reviewId, @Param("userId") Long userId);
    int updateReviewLikeCount(@Param("reviewId") Long reviewId, @Param("amount") int amount);

    // ===== 수정/삭제 =====
    int updateReview(@Param("reviewId") Long reviewId,
                     @Param("userId") Long userId,
                     @Param("dto") ReviewCreatedDTO dto);

    int deletePhotosByReviewId(@Param("reviewId") Long reviewId);
    int deleteReview(@Param("reviewId") Long reviewId, @Param("userId") Long userId);
    int adminDeleteReview(@Param("reviewId") Long reviewId);

    // ===== 메타/집계 =====
    Map<String, Object> findReviewMeta(@Param("reviewId") Long reviewId);

    int recomputeTargetStats(@Param("targetId") Long targetId,
                             @Param("targetType") String targetType);

    // ===== 신고 =====
    int insertReviewReport(@Param("reviewId") Long reviewId,
                           @Param("reporterUserId") Long reporterUserId);

    // 관리자용 : 신고 반려 시 신고 기록 삭제 (review_report 테이블)
    int deleteReviewReportByReviewId(@Param("reviewId") Long reviewId);


    int adminDeleteReviewIfReportedAtLeast(@Param("reviewId") Long reviewId,
                                           @Param("threshold") int threshold);

    // ===== 좋아요 상태 조회 =====
    List<Long> findLikedReviewIdsByTargetAndUser(
            @Param("targetType") String targetType,
            @Param("targetId") Long targetId,
            @Param("userId") Long userId
        );

    // =======================================================
    // [수정] 관리자 신고 리뷰 목록 조회 (페이지네이션 및 필터 적용)
    // =======================================================
    /**
     * 신고된 리뷰 목록을 조회합니다. 필터 조건(targetType)과 페이지네이션 조건(offset, limit)이 Map에 담겨 전달됩니다.
     * @param params 검색 조건 및 페이지네이션 정보가 담긴 Map
     * @return 신고된 리뷰 목록 DTO 리스트
     */
    List<ReportedReviewDTO> findReportedReviews(Map<String, Object> params);
    /**
     * 신고된 리뷰의 총 개수를 조회합니다. 필터 조건(targetType)이 Map에 담겨 전달됩니다.
     * 필터링된 총 개수는 페이지네이션 정보 계산에 사용됩니다.
     * @param params 필터 조건이 담긴 Map
     * @return 필터링 조건에 맞는 전체 신고 리뷰 개수
     */
    long countReportedReviews(Map<String, Object> params);

}
