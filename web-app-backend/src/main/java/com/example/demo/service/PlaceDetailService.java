package com.example.demo.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.demo.dto.SciencePlaceAdminRequestDto;
import com.example.demo.dto.StampRequestDTO;
import com.example.demo.dto.StampResponseDTO;
import com.example.demo.dto.PlaceResultDTO;
import com.example.demo.mapper.ContentMapper;
import com.example.demo.mapper.MappingMapper;
import com.example.demo.mapper.StampMapper;
import com.example.demo.vo.SciencePlace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.PlaceDetailDTO;
import com.example.demo.mapper.PlaceDetailMapper;

@Service
public class PlaceDetailService {

	@Autowired
	private PlaceDetailMapper placeDetailMapper;
    @Autowired
    private ContentMapper contentMapper;
    @Autowired
    private MappingMapper mappingMapper;

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
        params.put("userId", userId);
		
		// Mapper 메서드 호출 시, 3개의 값이 아닌 Map 객체 1개를 전달
		PlaceDetailDTO dto = placeDetailMapper.findPlaceDetailDTO(params);

        // 3-1. 만약 DTO가 null이면(결과가 없으면) 바로 반환
        if (dto == null) {
            return null;
        }

		return dto;
	}

    // --- [추가] Admin 읽기 기능 ---
    public List<PlaceResultDTO> findAllForAdmin() {
        return contentMapper.findAllForAdmin();
    }
    // --- [Admin 쓰기 기능] ---
    @Transactional
    public void createSciencePlace(SciencePlaceAdminRequestDto dto) {
        // 1. Place 엔티티 저장
        SciencePlace place = new SciencePlace();
        place.setPlaceName(dto.getPlaceName());
        place.setAddressDetail(dto.getAddressDetail());
        place.setMainImageUrl(dto.getMainImageUrl());
        if(dto.getLatitude() != null && !dto.getLatitude().isEmpty()) place.setLatitude(new BigDecimal(dto.getLatitude()));
        if(dto.getLongitude() != null && !dto.getLongitude().isEmpty()) place.setLongitude(new BigDecimal(dto.getLongitude()));
        place.setAdmissionFee(dto.getAdmissionFee());
        place.setOpeningHours(dto.getOpeningHours());
        place.setDescription(dto.getDescription());
        contentMapper.insert(place);
        Long newPlaceId = place.getId();
        // 2. [수정] 'mapping' 테이블에 학년/과목 정보 저장 (로직 활성화)
        Long gradeId = mappingMapper.getGradeIdByName(dto.getGrade());
        Long subCategoryId = mappingMapper.getSubCategoryIdBySubjectName(dto.getSubject());
        if (gradeId != null) {
            mappingMapper.insertPlaceGradeMapping(newPlaceId, gradeId);
        }
        if (subCategoryId != null) {
            mappingMapper.insertPlaceCurriculumMapping(newPlaceId, subCategoryId);
        }
    }
    @Transactional
    public void updateSciencePlace(Long id, SciencePlaceAdminRequestDto dto, String newImageUrl) {
        SciencePlace place = contentMapper.findById(id);
        if (place == null) {
            throw new RuntimeException("Place not found with id: " + id);
        }
        // 1. Place 엔티티 업데이트
        place.setPlaceName(dto.getPlaceName());
        place.setAddressDetail(dto.getAddressDetail());
        if(dto.getLatitude() != null && !dto.getLatitude().isEmpty()) place.setLatitude(new BigDecimal(dto.getLatitude()));
        if(dto.getLongitude() != null && !dto.getLongitude().isEmpty()) place.setLongitude(new BigDecimal(dto.getLongitude()));
        place.setAdmissionFee(dto.getAdmissionFee());
        place.setOpeningHours(dto.getOpeningHours());
        place.setDescription(dto.getDescription());
        if (newImageUrl != null) {
            place.setMainImageUrl(newImageUrl);
        }
        contentMapper.update(place);
        // 2. [수정] 'mapping' 정보 업데이트 (로직 활성화)
        // 2-1. 기존 매핑 정보 삭제
        mappingMapper.deletePlaceGradeMapping(id);
        mappingMapper.deletePlaceCurriculumMapping(id);
        // 2-2. 새 매핑 정보 조회 및 저장
        Long gradeId = mappingMapper.getGradeIdByName(dto.getGrade());
        Long subCategoryId = mappingMapper.getSubCategoryIdBySubjectName(dto.getSubject());
        if (gradeId != null) {
            mappingMapper.insertPlaceGradeMapping(id, gradeId);
        }
        if (subCategoryId != null) {
            mappingMapper.insertPlaceCurriculumMapping(id, subCategoryId);
        }
    }
    @Transactional
    public void deleteSciencePlace(Long id) {
        // [수정] 매핑 정보 삭제
        mappingMapper.deletePlaceGradeMapping(id);
        mappingMapper.deletePlaceCurriculumMapping(id);
        contentMapper.deleteById(id);
    }
}
