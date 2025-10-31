package com.example.demo.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

import com.example.demo.dto.WishListResponseDTO;
import com.example.demo.mapper.WishListMapper;

@Slf4j
@Service
public class WishListService {

	@Autowired
	private WishListMapper wishlistMapper;
	
	/**
     * 1. 찜하기 (추가)
     */
	public boolean addWishlist(Long userId, Long targetId, String targetType, String mainCategory, String gradeTag ) {
		
		// 1. 매퍼의 existsInWishlist (boolean)를 호출해서 검사
        boolean alreadyExists = wishlistMapper.existsInWishlist(userId, targetType, targetId);

        if (alreadyExists) {
            // 2. 중복이면 false를 컨트롤러에 반환
            return false;
        } else {
            // 3. 중복이 아니면 매퍼의 addWishlist (void)를 호출해서 삽입
    		wishlistMapper.addWishlist(userId, targetId, targetType, mainCategory, gradeTag);
            // 4. 성공했으므로 true를 컨트롤러에 반환
            return true;
        }
		
	}
	
	/**
     * 2. 찜 취소 (삭제)
     * removeWishlist는 int를 반환하도록 해서, 실제로 몇 줄이 삭제되었는지(보통 1줄) 확인하는 것이 더 좋다.
     */
	public int removeWishlist(Long userId, Long targetId) {
		return wishlistMapper.removeWishlist(userId, targetId);
		
	}
	
	/**
     * 3. 찜 여부 확인 (상세페이지용)
     */
	public boolean existsInWishlist(Long userId, String targetType, Long targetId) {
		return wishlistMapper.existsInWishlist(userId, targetType, targetId);
	}
	
	/**
     * 4. 내 찜 목록 조회 (마이페이지용)
     */
	public List<WishListResponseDTO> findMyWishlistByUserId(Long userId){
        // 1. 🟢 [로그 추가] 서비스 진입 확인
        log.info("[WishListService] findMyWishlistByUserId - 서비스 진입. userId: {}", userId);

        try {
            // 2. 🟢 [로그 추가] DB(Mapper) 호출 직전

            List<WishListResponseDTO> result = wishlistMapper.findMyWishlistByUserId(userId);

            // 3. 🟢 DB 호출 완료 (이 로그가 찍히면 성공)
            log.info("[WishListService] findMyWishlistByUserId - Mapper(DB) 호출 완료. 결과: {}건", result.size());
            return result;

        } catch (Exception e) {
            // 4. 🟢 예외 발생 시
            log.error("[WishListService] findMyWishlistByUserId - DB 조회 중 예외 발생!", e);
            throw e; // 예외를 컨트롤러로 다시 던짐
        }
    }
}
