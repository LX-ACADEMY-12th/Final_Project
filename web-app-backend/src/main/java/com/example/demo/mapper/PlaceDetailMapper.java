package com.example.demo.mapper;

import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.example.demo.dto.PlaceDetailDTO;

@Mapper
public interface PlaceDetailMapper {

	/**
	 * 장소ID를 받아서
	 * PlaceDetailDTO를 반환하는
	 * 메서드 정의
	 * */
	
	// 아이디를 통해 장소 상세정보를 가져올 메서드
	public PlaceDetailDTO findPlaceDetailDTO(Map<String, Object> params);
}
