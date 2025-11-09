package com.example.demo.controller;

import com.example.demo.dto.ReportedReviewDTO;
import com.example.demo.service.ReviewService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/admin/reviews")
public class ReviewAdminController {

    @Autowired
    private ReviewService reviewService;


    // =======================================================
    // 관리자용 신고 리뷰 관리 API
    // (보안 설정이 되었다는 가정 하에, 관리자 권한이 있는 사용자만 접근 가능)
    // =======================================================
    /**
     * [관리자] 신고된 리뷰 목록 조회 API
     */
    @GetMapping("/admin/reported")
    public ResponseEntity<List<ReportedReviewDTO>> getReportedReviews(Authentication authentication) {
        log.info("API CALL: [ADMIN] getReportedReviews - 임시 인증 해제 상태"); // 임시 로깅으로 대체
        try {
            List<ReportedReviewDTO> reviews = reviewService.findReportedReviews();
            return ResponseEntity.ok(reviews);
        } catch (Exception e) {
            log.error("[ADMIN] 신고 리뷰 조회 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    /**
     * [관리자] 신고 리뷰 '삭제' 처리 API (Soft Delete)
     * (리뷰 상태를 DELETED_BY_ADMIN으로 변경하고 신고 기록 삭제)
     */
    @PutMapping("/admin/reported/{reviewId}/delete")
    public ResponseEntity<String> deleteReportedReview(
            Authentication authentication,
            @PathVariable Long reviewId) {
        log.info("API CALL: [ADMIN] deleteReportedReview - reviewId: {} (임시 인증 해제 상태)", reviewId);
        try {
            int updatedCount = reviewService.processReportedReviewDeletion(reviewId);
            if (updatedCount > 0) {
                return ResponseEntity.ok("신고 리뷰 (ID: " + reviewId + ")가 관리자에 의해 삭제 처리되었습니다.");
            } else {
                return ResponseEntity.badRequest().body("이미 처리되었거나 존재하지 않는 리뷰입니다.");
            }
        } catch (Exception e) {
            log.error("[ADMIN] 신고 리뷰 삭제 처리 실패 - reviewId: {}", reviewId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("신고 리뷰 삭제 처리 중 오류 발생: " + e.getMessage());
        }
    }
    /**
     * [관리자] 신고 '반려' 처리 API
     * (리뷰는 유지하고, 신고 기록만 삭제)
     */
    @PutMapping("/admin/reported/{reviewId}/reject")
    public ResponseEntity<String> rejectReportedReview(
            Authentication authentication,
            @PathVariable Long reviewId) {
//          Long userId = getUserIdFromAuthentication(authentication);
//          if (userId == null) {
//              return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
//          }
//
//          log.info("API CALL: [ADMIN] rejectReportedReview - reviewId: {}, userId: {}", reviewId, userId);
        log.info("API CALL: [ADMIN] rejectReportedReview - reviewId: {} (임시 인증 해제 상태)", reviewId);
        try {
            int deletedCount = reviewService.rejectReportedReview(reviewId);
            if (deletedCount > 0) {
                return ResponseEntity.ok("신고 리뷰 (ID: " + reviewId + ")에 대한 신고가 반려 처리되었습니다.");
            } else {
                return ResponseEntity.badRequest().body("해당 리뷰에 대해 처리할 신고 기록이 없습니다.");
            }
        } catch (Exception e) {
            log.error("[ADMIN] 신고 리뷰 반려 처리 실패 - reviewId: {}", reviewId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("신고 리뷰 반려 처리 중 오류 발생: " + e.getMessage());
        }

    }
}


