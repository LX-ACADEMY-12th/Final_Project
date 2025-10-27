package com.example.demo.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.PlaceDetailDTO;
import com.example.demo.mapper.PlaceDetailMapper;

@Service
public class PlaceDetailService {

	@Autowired
	private PlaceDetailMapper placeDetailMapper;
	
	/**
     * 컨트롤러(Controller)가 호출할 메서드입니다.
     * 장소 ID를 받아서 상세 정보를 반환합니다.
     */
	public PlaceDetailDTO getfindPlaceDetails(
			Long placeId, String mainCategoryTags, 
			String subCategoryTags,String gradeTags) {
		
		// 파라미터를 담을 Map 생성
		Map<String, Object> params = new HashMap<>();
		
		// Map에 담을 파라미터를 "Key", "Value" 쌍으로 담는다.
		// Key의 이름은 Mapper.xml의 #{...} 과 같아야 한다.
		params.put("placeId", placeId);
		params.put("mainCategoryTags", mainCategoryTags);
		params.put("subCategoryTags", subCategoryTags);
		params.put("gradeTags", gradeTags);
		
		// Mapper 메서드 호출 시, 3개의 값이 아닌 Map 객체 1개를 전달
		PlaceDetailDTO dto = placeDetailMapper.findPlaceDetailDTO(params);
		
		return dto;
	}
}
