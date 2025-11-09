package com.example.demo.service;

import java.util.HashMap;
import java.util.Map;

import com.example.demo.dto.StampRequestDTO;
import com.example.demo.dto.StampResponseDTO;
import com.example.demo.mapper.StampMapper;
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

    @Autowired
    private StampMapper stampMapper;
	
	/**
     * 컨트롤러(Controller)가 호출할 메서드입니다.
     * 장소 ID를 받아서 상세 정보를 반환합니다.
     */
	public PlaceDetailDTO getfindPlaceDetails(
			Long placeId, Long userId, String mainCategoryTags,
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

        // 3-1. (방어 코드) 만약 DTO가 null이면(결과가 없으면) 바로 반환
        if (dto == null) {
            return null;
        }

        // 4. [순서 변경!] *이제부터* DTO에 isVisited 값을 설정합니다.
        // 비로그인 사용자(userId가 null 또는 0)는 무조건 false
        if (userId == null || userId == 0) {
            dto.setVisited(false);
        } else {
            // 로그인 사용자면 스탬프 DB 조회

            // A. 스탬프 매퍼에 보낼 요청 DTO 준비
            StampRequestDTO checkDTO = new StampRequestDTO();
            checkDTO.setUserId(userId);
            checkDTO.setTargetId(placeId); // (중요!)
            checkDTO.setTargetType("exhibition"); // (중요!)

            // B. 매퍼 호출
            StampResponseDTO existingStamp = stampMapper.findStampByUserIdAndTarget(checkDTO);

            // C. 결과에 따라 isVisited 값 설정
            if (existingStamp != null) {
                // 이미 방문한 기록이 있으면
                dto.setVisited(true);
            } else {
                // 방문 기록이 없으면
                dto.setVisited(false);
            }
        }

		return dto;
	}
}
