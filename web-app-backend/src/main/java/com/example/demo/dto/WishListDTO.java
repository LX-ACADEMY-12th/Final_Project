package com.example.demo.dto;

import lombok.Data;

@Data
public class WishListDTO {

	// 전시 or 장소
	private String targetType;
	
	// 찜할 대상의 아이디
	private Long targetId;
	
	// (userId는 나중에 시큐리티에서 가져오므로 DTO에 포함시킬 필요가 없습니다.)
}
