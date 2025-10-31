package com.example.demo.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

import com.example.demo.dto.WishListRequestDTO;
import com.example.demo.dto.WishListResponseDTO;

import com.example.demo.service.WishListService;

@Slf4j
@RestController
@RequestMapping("/api/wishlist")
public class WishListController {

    @Autowired
    private WishListService wishlistService;

    /**
     * 상세 페이지에서 찜하는 api (중복 검사 포함)
     */
    @PostMapping
    public ResponseEntity<String> addWishlist(
            @RequestBody WishListRequestDTO wishListRequestDTO,
            Authentication authentication
    ) {

        // 헬퍼 메서드를 호출하여 userId 추출
        Long userId = getUserIdFromAuthentication(authentication);
        if (userId == null) {
            // 헬퍼 메서드가 null을 반환하면 (인증 실패 또는 파싱 오류)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        boolean addedSuccessfully = wishlistService.addWishlist(
                userId, // 헬퍼 메서드가 반환한 ID 사용
                wishListRequestDTO.getTargetId(),
                wishListRequestDTO.getTargetType(),
                wishListRequestDTO.getMainCategory(),
                wishListRequestDTO.getGradeTag()
        );

        if (addedSuccessfully) {
            return ResponseEntity.ok("찜 목록에 추가되었습니다.");
        } else {
            return ResponseEntity.status(409).body("이미 찜한 항목입니다.");
        }
    }

    /**
     * 2. 찜 취소 (삭제)
     */
    @DeleteMapping
    public ResponseEntity<String> removeWishlist(
            @RequestBody WishListRequestDTO wishListRequestDTO,
            Authentication authentication
    ) {

        // ⬅️ 헬퍼 메서드 호출
        Long userId = getUserIdFromAuthentication(authentication);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        int affectedRows = wishlistService.removeWishlist(
                userId, // 헬퍼 메서드가 반환한 ID 사용
                wishListRequestDTO.getTargetId()
        );

        if (affectedRows > 0) {
            return ResponseEntity.ok("찜 목록에서 삭제되었습니다.");
        } else {
            return ResponseEntity.status(404).body("삭제할 찜 항목을 찾을 수 없습니다.");
        }
    }

    /**
     * 3. 내 찜 목록 조회 (마이페이지 /likePlace 용)
     */
    @GetMapping("/my-list")
    public ResponseEntity<?> getMyWishlist(
            Authentication authentication
    ) {
        log.info("[API GET] /api/wishlist/my-list - 찜 목록 조회 요청 진입.");

        // ⬅️ 헬퍼 메서드 호출
        Long userId = getUserIdFromAuthentication(authentication);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        List<WishListResponseDTO> myList = wishlistService.findMyWishlistByUserId(userId); // ⬅️ 헬퍼 메서드가 반환한 ID 사용

        return ResponseEntity.ok(myList);
    }
    
    /**
     * 4. 내가 찜한 여부 확인 api
     */
    @GetMapping("/{targetType}/{targetId}/status")
    public ResponseEntity<?> checkWishlistStatus(@PathVariable String targetType, 
    									@PathVariable Long targetId, Authentication authentication) {
    	log.info("[API GET] /api/wishlist/my-list - 찜 목록 조회 요청 진입.");

        // ⬅️ 헬퍼 메서드 호출
        Long userId = getUserIdFromAuthentication(authentication);
        if (userId == null) {
        	return ResponseEntity.ok(Map.of("isWished", false));
        }
        boolean isWished = wishlistService.existsInWishlist(userId, targetType, targetId);
        
        // 4. 프론트엔드가 쓰기 편하게 JSON ({"isWished": true}) 으로 반환
        return ResponseEntity.ok(Map.of("isWished", isWished));
    }

    /**
     * 🟢  Authentication 객체에서 'userId' (Long)를 추출하는 헬퍼 메서드
     * (이 부분은 그대로 유지)
     */
    private Long getUserIdFromAuthentication(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) { // ⬅️ [개선] !isAuthenticated() 체크 추가
            log.warn("인증 정보가 없거나 유효하지 않습니다.");
            return null;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            String userIdStr = ((UserDetails) principal).getUsername();
            try {
                return Long.parseLong(userIdStr);
            } catch (NumberFormatException e) {
                log.error("Authentication principal이 Long 타입으로 변환 불가: {}", userIdStr, e);
                return null;
            }
        } else if (principal instanceof String && !principal.equals("anonymousUser")) { // ⬅️ [개선] 익명 사용자 체크
            try {
                return Long.parseLong((String) principal);
            } catch (NumberFormatException e) {
                log.error("Authentication principal이 Long 타입으로 변환 불가: {}", principal, e);
                return null;
            }
        } else if (principal.equals("anonymousUser")) {
            log.warn("익명 사용자(anonymousUser)의 요청입니다.");
            return null;
        }

        log.warn("알 수 없는 Principal 타입: {}", principal.getClass().getName());
        return null;
    }
}