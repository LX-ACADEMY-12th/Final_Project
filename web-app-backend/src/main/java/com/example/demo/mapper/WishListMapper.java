package com.example.demo.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.demo.dto.WishListResponseDTO;

@Mapper
public interface WishListMapper {

	/**
     * 1. 찜하기 (추가)
     */
	void addWishlist(
        @Param("userId") Long userId,
        @Param("targetId") Long targetId,
        @Param("targetType") String targetType,
        @Param("mainCategory") String mainCategory,
        @Param("gradeTag") String gradeTag);
	
	/**
     * 2. 찜 취소 (삭제)
     * removeWishlist는 int를 반환하도록 해서, 실제로 몇 줄이 삭제되었는지(보통 1줄) 확인하는 것이 더 좋다.
     */
	int removeWishlist(@Param("userId") Long userId,
            @Param("targetId") Long targetId);
	
	/**
     * 3. 찜 여부 확인 (상세페이지용)
     */
	boolean existsInWishlist(@Param("userId") Long userId, 
            @Param("targetType") String targetType, 
            @Param("targetId") Long targetId);
	
	/**
     * 4. 내 찜 목록 조회 (마이페이지용)
     */
	List<WishListResponseDTO> findMyWishlistByUserId(@Param("userId") Long userId);
}
