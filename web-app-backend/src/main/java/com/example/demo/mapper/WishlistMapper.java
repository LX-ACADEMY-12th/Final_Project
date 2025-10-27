package com.example.demo.mapper;

import com.example.demo.dto.WishlistItemDTO; // 아래 DTO 정의 필요
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface WishlistMapper {

    // 특정 사용자가 좋아요 한 모든 항목 조회 (타입과 ID)
    List<WishlistItemDTO> findLikedItemsByUserId(@Param("userId") Long userId);

}
