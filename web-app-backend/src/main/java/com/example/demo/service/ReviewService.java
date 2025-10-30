package com.example.demo.service;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// GCP 및 파일 처리를 위한 import
import java.io.IOException;
import java.util.UUID;
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
	
	@Value("${gcp.storage.bucket-name}")
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

	 				String photoUrl = "https://storage.googleapis.com/" + bucketName + "/" + blobName;

	 				reviewMapper.insertReviewPhoto(generatedReviewId, photoUrl, sequence);

	 				sequence++;

	 			} catch (IOException e) {
	 				throw new RuntimeException("GCP Storage 파일 업로드 실패", e);
	 			}
	 		}
	 	} // 👈 if (files != null...) 블록 끝
	    
	    // ⭐️ [수정] (버그 1 수정)
	    // 통계 재계산 로직을 if 블록 밖으로 이동시켰습니다.
	    reviewMapper.recomputeTargetStats(dto.getTargetId(), dto.getTargetType());
	}
	
	
	/**
     * 리뷰 목록 조회
     */
	public PageResponseDTO<ReviewResponseDTO> getfindTargetReview(
            Long targetId, String targetType, int page, int size) {
		
        // 1. ⭐️ [추가] 전체 개수 조회 (Mapper에 countReviewsByTarget가 추가되었다고 가정)
        long totalElements = reviewMapper.countReviewsByTarget(targetId, targetType);

        // 2. ⭐️ [추가] 총 페이지 수 계산
        int totalPages = (totalElements == 0) ? 1 : (int) Math.ceil((double) totalElements / size);
        
        // 3. ⭐️ [추가] offset 계산
        int offset = (page - 1) * size;

        // 4. ⭐️ [추가] Mapper에 넘길 파라미터 Map 생성
        Map<String, Object> params = new HashMap<>();
        params.put("targetId", targetId);
        params.put("targetType", targetType);
        params.put("size", size);
        params.put("offset", offset);

		// 5. ⭐️ [수정] (N+1 발생 지점) - Map을 파라미터로 넘겨 현재 페이지 리뷰 조회
        // (Mapper 인터페이스가 Map<String, Object> params를 받도록 수정되었다고 가정)
		List<ReviewResponseDTO> reviews = reviewMapper.findReviewsByTarget(params);
		
        // 6. (수정 불필요) N+1 해결 로직 (보내주신 코드와 동일)
		if (!reviews.isEmpty()) {
            // 6-1) reviewId 모으기
            List<Long> ids = reviews.stream().map(ReviewResponseDTO::getReviewId).toList();
            
            // 6-2) 사진 배치 조회
            List<Map<String, Object>> rows = reviewMapper.findPhotosByReviewIds(ids);
            
            // 6-3) reviewId -> List<String> 맵 구성
            Map<Long, List<String>> photosByReview = new HashMap<>();
            for (Map<String, Object> r : rows) {
                Long rid = ((Number) r.get("review_id")).longValue();
                String url = (String) r.get("photo_url");
                photosByReview.computeIfAbsent(rid, k -> new ArrayList<>()).add(url);
            }
            
            // 6-4) DTO에 세팅
            for (ReviewResponseDTO dto : reviews) {
                dto.setPhotoUrls(photosByReview.getOrDefault(dto.getReviewId(), List.of()));
            }
        }
		
        // 7. ⭐️ [수정] List 대신 PageResponseDTO로 포장해서 반환
		return new PageResponseDTO<>(
            reviews,        // content
            totalPages,     // totalPages
            totalElements,  // totalElements
            page,           // pageNumber
            size            // pageSize
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
		
        // 1. 텍스트 및 별점 수정
		int rows = reviewMapper.updateReview(reviewId, userId, dto);
	    if (rows == 0) throw new IllegalArgumentException("리뷰 수정 불가(권한/존재)");

        // 2. ⭐️ [수정] 파일 처리 로직 구현
        // 'files' 파라미터가 null이 아닌 경우에만 사진 관련 로직을 실행합니다.
        // (프론트엔드에서 사진을 수정하지 않으면 'files'를 null로 보내야 함)
        if (files != null) {
            
            // --- 2-1. 기존 GCS 파일 및 DB 레코드 삭제 ---
            
            // 2-1-1. DB에서 기존 사진 URL 목록 조회
            List<String> oldPhotoUrls = reviewMapper.findPhotosByReviewId(reviewId);

            // 2-1-2. GCS에서 실제 파일 삭제 (헬퍼 메서드 getBlobNameFromUrl 사용)
            try {
                for (String photoUrl : oldPhotoUrls) {
                    String blobName = getBlobNameFromUrl(photoUrl);
                    if (blobName != null) {
                        BlobId blobId = BlobId.of(bucketName, blobName);
                        storage.delete(blobId);
                    }
                }
            } catch (Exception e) {
                // GCS 삭제 실패는 일단 로그만 남기고 DB 작업은 계속 진행
                // (DB 트랜잭션 롤백은 불필요)
                System.err.println("GCS 기존 파일 삭제 실패 (리뷰ID: " + reviewId + "): " + e.getMessage());
            }
            
            // 2-1-3. DB에서 기존 review_photo 레코드 삭제 (Mapper 추가 필요)
            // (주의: 이 메서드는 ReviewMapper.xml에 새로 추가해야 합니다.)
            reviewMapper.deletePhotosByReviewId(reviewId);


            // --- 2-2. 새 GCS 파일 업로드 및 DB 레코드 추가 ---
            // (createReview의 파일 업로드 로직 재활용)
            
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
                        
                    // GCS에 파일 생성(업로드)
                    storage.create(blobInfo, file.getBytes());

                    String photoUrl = "https://storage.googleapis.com/" + bucketName + "/" + blobName;

                    // DB에 새 사진 정보 삽입
                    reviewMapper.insertReviewPhoto(reviewId, photoUrl, sequence);

                    sequence++;

                } catch (IOException e) {
                    // IO 예외 발생 시 트랜잭션 롤백을 위해 런타임 예외로 전환
                    throw new RuntimeException("GCP Storage 새 파일 업로드 실패 (리뷰 수정 중)", e);
                }
            }
        }
        // 'files'가 null이면 (프론트에서 안 보냈으면) 사진 관련 작업을 모두 건너뜁니다.

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

            // 1. GCS에서 실제 파일 삭제 / 스토리지 정리
            try {
                List<String> photoUrls = reviewMapper.findPhotosByReviewId(reviewId);
                for (String photoUrl : photoUrls) {
                    String blobName = getBlobNameFromUrl(photoUrl);
                    if (blobName != null) {
                        BlobId blobId = BlobId.of(bucketName, blobName);
                        storage.delete(blobId);
                    }
                }
            } catch (Exception e) {
                System.err.println("GCS 파일 삭제 실패 (리뷰ID: " + reviewId + "): " + e.getMessage());
            }

            // 3. DB에서 부모 레코드(리뷰 본문) 삭제
		    int rows = reviewMapper.deleteReview(reviewId, userId);
		    if (rows == 0) throw new IllegalArgumentException("삭제 불가(권한/존재)");

            // 4. 통계 재계산
		    reviewMapper.recomputeTargetStats(targetId, type);
	}
	
	// 신고 추가 + 임계치 도달 시에만 조건부 삭제 (동시성 안전)
	@Transactional(rollbackFor = Exception.class)
	public void insertReviewReport(Long reviewId, Long userId) {
	    final int THRESHOLD = 10;

	    // 0) 대상 메타 (없으면 예외)
	    Map<String, Object> meta = reviewMapper.findReviewMeta(reviewId);
	    if (meta == null) throw new IllegalArgumentException("신고 대상 없음");

	    Long targetId = ((Number) meta.get("targetId")).longValue();
	    String targetType = (String) meta.get("targetType");

	    // 1) (선택) 사진 URL 미리 확보 — 실제 삭제 확정 시에만 GCS 삭제
	    List<String> photoUrls = reviewMapper.findPhotosByReviewId(reviewId);

	    // 2) 중복 검사 없이 곧장 INSERT → PK 충돌이면 "이미 신고함"
	    try {
	        reviewMapper.insertReviewReport(reviewId, userId);
	    } catch (org.springframework.dao.DuplicateKeyException e) {
	        throw new IllegalStateException("이미 신고함");
	    }

	    // 3) 임계치 이상일 때만 '조건부 한 방' 삭제 시도 (0 또는 1 반환)
	    int deleted = reviewMapper.adminDeleteReviewIfReportedAtLeast(reviewId, THRESHOLD);

	    // 4) 실제로 삭제되었을 때만 GCS 삭제 + 집계 재계산
	    if (deleted > 0) {
	        try {
	            for (String url : photoUrls) {
	                String blob = getBlobNameFromUrl(url);
	                if (blob != null) storage.delete(BlobId.of(bucketName, blob));
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
        return reviewMapper.findAllPhotosByTarget(targetType, targetId);
    }
	
    // ⭐️ [수정] (버그 2 수정) GCS URL 파싱 헬퍼 메서드 추가
    private String getBlobNameFromUrl(String url) {
        String prefix = "https://storage.googleapis.com/" + bucketName + "/";
        if (url != null && url.startsWith(prefix)) {
            return url.substring(prefix.length());
        }
        System.err.println("유효하지 않은 GCS URL입니다: " + url);
        return null;
    }
}