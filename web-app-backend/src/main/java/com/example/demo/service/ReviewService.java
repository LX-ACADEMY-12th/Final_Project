package com.example.demo.service;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors; // 👈 [추가] Stream API import

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// GCP 및 파일 처리를 위한 import
import java.io.IOException;
import java.util.UUID;
import java.net.URL; // 👈 [추가] Signed URL 생성을 위해
import java.util.concurrent.TimeUnit; // 👈 [추가] Signed URL 시간 설정을 위해

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.example.demo.dto.PageResponseDTO;
import com.example.demo.dto.PhotoThumbDTO;
import com.example.demo.dto.PlaceDetailDTO;
import com.example.demo.dto.ReviewCreatedDTO;
import com.example.demo.dto.ReviewResponseDTO;
import com.example.demo.mapper.ReviewMapper;

@Service
public class ReviewService {

    @Autowired
    private ReviewMapper reviewMapper;

    @Autowired
    private Storage storage;

    // ⭐️ [수정] application-dev.properties의 키와 맞춤 (gcs.bucket-name)
    @Value("${gcs.bucket-name}") // (application-dev.properties에 gcs.bucket-name=science_book 설정 필요)
    private String bucketName;

    /**
     * 리뷰 생성
     */
    @Transactional
    public void createReview(ReviewCreatedDTO dto, Long userId, List<MultipartFile> files) {

        reviewMapper.insertReview(dto, userId);

        Long generatedReviewId = dto.getReviewId();

        // 3. GCS 파일 저장 로직 (파일이 있을 경우에만)
        if (files != null && !files.isEmpty()) {

            int sequence = 0; // 사진 순서
            for (MultipartFile file : files) {
                if (file.isEmpty())
                    continue;

                String originalName = file.getOriginalFilename();
                String extension = "";
                if (originalName != null && originalName.contains(".")) {
                    extension = originalName.substring(originalName.lastIndexOf("."));
                }
                String blobName = "reviews/" + UUID.randomUUID().toString() + extension;

                try {
                    BlobId blobId = BlobId.of(bucketName, blobName);
                    BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                            .setContentType(file.getContentType())
                            .build();

                    storage.create(blobInfo, file.getBytes());

                    // 🔴 [수정] DB에는 전체 URL 대신 GCS 객체 이름(blobName)을 저장합니다.
                    // String photoUrl = "https://storage.googleapis.com/" + bucketName + "/" + blobName; // (삭제)
                    reviewMapper.insertReviewPhoto(generatedReviewId, blobName, sequence);

                    sequence++;

                } catch (IOException e) {
                    throw new RuntimeException("GCP Storage 파일 업로드 실패", e);
                }
            }
        }

        reviewMapper.recomputeTargetStats(dto.getTargetId(), dto.getTargetType());
    }


    /**
     * 리뷰 목록 조회
     */
    public PageResponseDTO<ReviewResponseDTO> getfindTargetReview(
            Long targetId, String targetType, int page, int size) {

        long totalElements = reviewMapper.countReviewsByTarget(targetId, targetType);
        int totalPages = (totalElements == 0) ? 1 : (int) Math.ceil((double) totalElements / size);
        int offset = (page - 1) * size;

        Map<String, Object> params = new HashMap<>();
        params.put("targetId", targetId);
        params.put("targetType", targetType);
        params.put("size", size);
        params.put("offset", offset);

        List<ReviewResponseDTO> reviews = reviewMapper.findReviewsByTarget(params);

        if (!reviews.isEmpty()) {
            List<Long> ids = reviews.stream().map(ReviewResponseDTO::getReviewId).toList();
            List<Map<String, Object>> rows = reviewMapper.findPhotosByReviewIds(ids);

            Map<Long, List<String>> photosByReview = new HashMap<>();
            for (Map<String, Object> r : rows) {
                Long rid = ((Number) r.get("review_id")).longValue();

                // 🔴 [수정] DB에서 가져온 값은 이제 GCS 객체 이름(objectName)입니다.
                String objectName = (String) r.get("photo_url");

                // 🟢 [추가] 객체 이름을 Signed URL로 변환합니다.
                String signedUrl = generateSignedUrl(objectName);

                if (signedUrl != null) {
                    photosByReview.computeIfAbsent(rid, k -> new ArrayList<>()).add(signedUrl);
                }
            }

            for (ReviewResponseDTO dto : reviews) {
                dto.setPhotoUrls(photosByReview.getOrDefault(dto.getReviewId(), List.of()));
            }
        }

        return new PageResponseDTO<>(
                reviews, totalPages, totalElements, page, size
        );
	}
	
	// 리뷰 후기 아래 사진들
	public List<PhotoThumbDTO> findPhotoThumbnailsByTarget(String targetType, Long targetId, int limit) {
	    // ⭐️ 매퍼를 호출합니다.
	    return reviewMapper.findPhotoThumbnailsByTarget(targetType, targetId, limit);
	}
	
	
	// 리뷰에 좋아요 누른 아이디..
	public List<Long> findLikedReviewIds(Long targetId, String targetType, Long viewerUserId) {
        java.util.Map<String, Object> params = new java.util.HashMap<>();
        params.put("targetId", targetId);
        params.put("targetType", targetType);
        params.put("viewerUserId", viewerUserId);
        return reviewMapper.findLikedReviewIdsByTargetAndUser(params);
    }

    // 리뷰 좋아요 추가
    @Transactional
    public void addLike(Long reviewId, Long userId) {
        int likeCount = reviewMapper.existsReviewLike(reviewId, userId);
        if(likeCount == 0) {
            reviewMapper.insertReviewLike(reviewId, userId);
            reviewMapper.updateReviewLikeCount(reviewId, 1);
        }
    }

    // 리뷰 좋아요 삭제
    @Transactional
    public void deleteLike(Long reviewId, Long userId) {
        int affectedRows = reviewMapper.deleteReviewLike(reviewId, userId);
        if (affectedRows > 0) {
            reviewMapper.updateReviewLikeCount(reviewId, -1);
        }
    }

    /**
     * 리뷰 수정
     */
    @Transactional
    public void updateReview(Long reviewId, Long userId, ReviewCreatedDTO dto, List<MultipartFile> files) {

        int rows = reviewMapper.updateReview(reviewId, userId, dto);
        if (rows == 0) throw new IllegalArgumentException("리뷰 수정 불가(권한/존재)");

        if (files != null) {

            // --- 2-1. 기존 GCS 파일 및 DB 레코드 삭제 ---

            // 🔴 [수정] DB에서 기존 GCS 객체 이름(blobName) 목록 조회
            List<String> oldObjectNames = reviewMapper.findPhotosByReviewId(reviewId);

            try {
                // 🔴 [수정] URL 파싱 대신 객체 이름으로 GCS 파일 바로 삭제
                for (String blobName : oldObjectNames) {
                    if (blobName != null && !blobName.isEmpty()) {
                        BlobId blobId = BlobId.of(bucketName, blobName);
                        storage.delete(blobId);
                    }
                }
            } catch (Exception e) {
                System.err.println("GCS 기존 파일 삭제 실패 (리뷰ID: " + reviewId + "): " + e.getMessage());
            }

            reviewMapper.deletePhotosByReviewId(reviewId);

            // --- 2-2. 새 GCS 파일 업로드 및 DB 레코드 추가 ---
            int sequence = 0;
            for (MultipartFile file : files) {
                if (file.isEmpty()) continue;

                String originalName = file.getOriginalFilename();
                String extension = "";
                if (originalName != null && originalName.contains(".")) {
                    extension = originalName.substring(originalName.lastIndexOf("."));
                }
                String blobName = "reviews/" + UUID.randomUUID().toString() + extension;

                try {
                    BlobId blobId = BlobId.of(bucketName, blobName);
                    BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                            .setContentType(file.getContentType())
                            .build();

                    storage.create(blobInfo, file.getBytes());

                    // 🔴 [수정] DB에 새 사진의 객체 이름(blobName) 삽입
                    reviewMapper.insertReviewPhoto(reviewId, blobName, sequence);
                    sequence++;

                } catch (IOException e) {
                    throw new RuntimeException("GCP Storage 새 파일 업로드 실패 (리뷰 수정 중)", e);
                }
            }
        }

        // 3. 통계 재계산
        Map<String, Object> meta = reviewMapper.findReviewMeta(reviewId);
        if (meta == null) return;

        Long targetId = ((Number) meta.get("targetId")).longValue();
        String type   = (String) meta.get("targetType");

        reviewMapper.recomputeTargetStats(targetId, type);
    }

    // 리뷰 삭제
    @Transactional
    public void deleteReview(Long reviewId, Long userId) {
        Map<String, Object> meta = reviewMapper.findReviewMeta(reviewId);
        if (meta == null) throw new IllegalArgumentException("리뷰 없음");

        Long targetId = ((Number) meta.get("targetId")).longValue();
        String type   = (String) meta.get("targetType");

        // 1. GCS에서 실제 파일 삭제
        try {
            // 🔴 [수정] DB에서 객체 이름(blobName) 목록 조회
            List<String> objectNames = reviewMapper.findPhotosByReviewId(reviewId);
            // 🔴 [수정] URL 파싱 대신 객체 이름으로 GCS 파일 바로 삭제
            for (String blobName : objectNames) {
                if (blobName != null && !blobName.isEmpty()) {
                    BlobId blobId = BlobId.of(bucketName, blobName);
                    storage.delete(blobId);
                }
            }
        } catch (Exception e) {
            System.err.println("GCS 파일 삭제 실패 (리뷰ID: " + reviewId + "): " + e.getMessage());
        }

        int rows = reviewMapper.deleteReview(reviewId, userId);
        if (rows == 0) throw new IllegalArgumentException("삭제 불가(권한/존재)");

        reviewMapper.recomputeTargetStats(targetId, type);
    }

    // 신고 추가 + 임계치 도달 시에만 조건부 삭제
    @Transactional(rollbackFor = Exception.class)
    public void insertReviewReport(Long reviewId, Long userId) {
        final int THRESHOLD = 10;

        Map<String, Object> meta = reviewMapper.findReviewMeta(reviewId);
        if (meta == null) throw new IllegalArgumentException("신고 대상 없음");

        Long targetId = ((Number) meta.get("targetId")).longValue();
        String targetType = (String) meta.get("targetType");

        // 🔴 [수정] DB에서 객체 이름(blobName) 목록 미리 확보
        List<String> objectNames = reviewMapper.findPhotosByReviewId(reviewId);

        try {
            reviewMapper.insertReviewReport(reviewId, userId);
        } catch (org.springframework.dao.DuplicateKeyException e) {
            throw new IllegalStateException("이미 신고함");
        }

        int deleted = reviewMapper.adminDeleteReviewIfReportedAtLeast(reviewId, THRESHOLD);

        if (deleted > 0) {
            try {
                // 🔴 [수정] URL 파싱 대신 객체 이름으로 GCS 파일 바로 삭제
                for (String blobName : objectNames) {
                    if (blobName != null && !blobName.isEmpty()) {
                        storage.delete(BlobId.of(bucketName, blobName));
                    }
                }
            } catch (Exception e) {
                System.err.println("GCS 파일 삭제 실패 (신고 누적/리뷰ID: " + reviewId + "): " + e.getMessage());
            }
            reviewMapper.recomputeTargetStats(targetId, targetType);
        }
    }

    /**
     * 특정 대상(장소/전시)의 모든 사진 URL 목록 조회
     */
    public List<String> findAllPhotosByTarget(String targetType, Long targetId) {
        // 🔴 [수정] DB에서 객체 이름(blobName) 목록 조회
        List<String> objectNames = reviewMapper.findAllPhotosByTarget(targetType, targetId);

        // 🟢 [추가] 조회된 모든 객체 이름을 Signed URL로 변환하여 반환
        return objectNames.stream()
                .map(this::generateSignedUrl) // 각 objectName을 Signed URL로 변환
                .filter(url -> url != null) // 변환 실패(null)한 경우 제외
                .collect(Collectors.toList());
    }

    // 🔴 [삭제] 불필요한 GCS URL 파싱 헬퍼 메서드 삭제
    // private String getBlobNameFromUrl(String url) { ... }

    // 🟢 [추가] Signed URL 생성을 위한 헬퍼 메서드
    /**
     * GCS 객체 이름(blobName)을 15분간 유효한 Signed URL로 변환합니다.
     * @param objectName (예: "reviews/uuid-filename.png")
     * @return 15분간 유효한 URL 문자열 (실패 시 null)
     */
    private String generateSignedUrl(String objectName) {
        if (objectName == null || objectName.isEmpty()) {
            return null;
        }

        try {
            BlobInfo blobInfo = BlobInfo.newBuilder(BlobId.of(bucketName, objectName)).build();

            // 15분 제한 시간 설정 (V4 서명 방식)
            URL signedUrl = storage.signUrl(blobInfo, 15, TimeUnit.MINUTES, Storage.SignUrlOption.withV4Signature());

            return signedUrl.toString();
        } catch (Exception e) {
            // GCS 통신 중 오류 발생 시 로그 남기기
            System.err.println("Signed URL 생성 실패 (Object: " + objectName + "): " + e.getMessage());
            return null; // 실패 시 null 반환
        }
    }
}