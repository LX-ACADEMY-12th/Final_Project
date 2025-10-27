package com.example.demo.mapper;

import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.example.demo.dto.ExhibitionDetailDTO;

@Mapper
public interface ExhibitionDetailMapper {
	
	/**
     * 전시 ID를 받아서
     * (이것이 나중에 컨트롤러가 URL에서 받은 그 'id'입니다)
     * * ExhibitionDetailDTO를 반환하는
     * 메서드를 '정의'합니다.
     */
	
	// 아이디를 통해 전시 상세정보를 가져올 메서드
	public ExhibitionDetailDTO findExhibitionById(Map<String, Object> params);

}
