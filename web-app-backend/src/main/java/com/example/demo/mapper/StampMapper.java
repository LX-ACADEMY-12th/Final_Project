package com.example.demo.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.example.demo.dto.StampRequestDTO;
import com.example.demo.dto.StampResponseDTO;

@Mapper
public interface StampMapper {
	
	public int addToVisitStamp(StampResponseDTO dto);
	
	public List<StampResponseDTO> findStampsByUserId(Long userId);
	
	// 중복 확인
	public StampResponseDTO findStampByUserIdAndTarget(StampRequestDTO reqDto);
	
}
