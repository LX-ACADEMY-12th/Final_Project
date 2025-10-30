package com.example.demo.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.dto.UserWishListDTO;
import com.example.demo.dto.WishListDTO;
import com.example.demo.mapper.WishListMapper;

@Service
public class WishListService {

	@Autowired
	private WishListMapper wishlistMapper;
	
	/**
     * 1. 찜하기 (추가)
     */
	public boolean addWishlist(Long userId, String targetType, Long targetId) {
		
		// 1. 매퍼의 existsInWishlist (boolean)를 호출해서 검사
        boolean alreadyExists = wishlistMapper.existsInWishlist(userId, targetType, targetId);

        if (alreadyExists) {
            // 2. 중복이면 false를 컨트롤러에 반환
            return false;
        } else {
            // 3. 중복이 아니면 매퍼의 addWishlist (void)를 호출해서 삽입
    		wishlistMapper.addWishlist(userId, targetType, targetId);
            // 4. 성공했으므로 true를 컨트롤러에 반환
            return true;
        }
		
	}
	
	/**
     * 2. 찜 취소 (삭제)
     * removeWishlist는 int를 반환하도록 해서, 실제로 몇 줄이 삭제되었는지(보통 1줄) 확인하는 것이 더 좋다.
     */
	public int removeWishlist(Long userId, String targetType, Long targetId) {
		return wishlistMapper.removeWishlist(userId, targetType, targetId);
		
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
	public List<UserWishListDTO> findMyWishlistByUserId(Long userId){
		return wishlistMapper.findMyWishlistByUserId(userId);
		
	}
}
