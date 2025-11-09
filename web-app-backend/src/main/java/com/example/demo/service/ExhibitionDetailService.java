package com.example.demo.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.demo.dto.*;
import com.example.demo.mapper.*;
import com.example.demo.vo.Exhibition;
import com.example.demo.vo.ExhibitionHall;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

@Service
public class ExhibitionDetailService {

	@Autowired
	private ExhibitionDetailMapper exhibitionDetailMapper;

    @Autowired
    private StampMapper stampMapper;

    @Autowired
    private ExhibitionMapper exhibitionMapper;

    @Autowired
    private ExhibitionHallMapper exhibitionHallMapper;

    @Autowired
    private MappingMapper mappingMapper;
	
	/**
     * 컨트롤러(Controller)가 호출할 메서드
     * 전시관 ID를 받아서 상세 정보를 반환
     */
	public ExhibitionDetailDTO getfindExhibitionDetails(
            Long exhibitionId, Long userId,
            String mainCategoryTags, String subCategoryTags,String gradeTags) {
		
		// 파라미터를 담을 Map 생성
		Map<String, Object> params = new HashMap<>();
		
		// Map에 파라미터를 "Key", "Value" 쌍으로 담습니다.
        // "Key" 이름(문자열)이 Mapper.xml의 #{...} 이름과 일치해야 합니다.
		// 전시관 ID/ 메인 / 서브 / 학년
		params.put("exhibitionHallId", exhibitionId);
		params.put("mainCategoryTags", mainCategoryTags);
		params.put("subCategoryTags", subCategoryTags);
		params.put("gradeTags", gradeTags);

		// Mapper 메서드 호출 시, 값이 아닌 'Map' 객체 1개를 전달합니다.
		ExhibitionDetailDTO dto = exhibitionDetailMapper.findExhibitionById(params);

        // 만약 DTO가 null이면(결과가 없으면) 바로 반환
        if (dto == null) {
            return null;
        }

        // 이제부터 DTO에 isVisited 값을 설정합니다.
        // 비로그인 사용자(userId가 null 또는 0)는 무조건 false
        if (userId == null || userId == 0) {
            dto.setVisited(false);
        } else {
            // 로그인 사용자면 스탬프 DB 조회

            // A. 스탬프 매퍼에 보낼 요청 DTO 준비
            StampRequestDTO checkDTO = new StampRequestDTO();
            checkDTO.setUserId(userId);
            checkDTO.setTargetId(exhibitionId); // (중요!)
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

		// 4. 컨트롤러에게 결과 반환
		return dto;
	}


    // --- Admin 읽기 기능 ---
    public List<PlaceResultDTO> findAllForAdmin() {
        return exhibitionMapper.findAllForAdmin();
    }
    // --- [Admin 쓰기 기능] ---
    @Transactional
    public void createExhibition(ExhibitionAdminRequestDto dto) {
        ExhibitionHall hall = exhibitionHallMapper.findByName(dto.getHallName());
        Long hallId;
        // 1. 전시관(Hall) 처리 로직 (기존과 동일)
        if (hall == null) {
            ExhibitionHall newHall = new ExhibitionHall();
            newHall.setHallName(dto.getHallName());
            newHall.setAddressDetail(dto.getAddressDetail());
            exhibitionHallMapper.insert(newHall);
            hallId = newHall.getId();
        } else {
            hallId = hall.getId();
            if (dto.getAddressDetail() != null && !dto.getAddressDetail().equals(hall.getAddressDetail())) {
                hall.setAddressDetail(dto.getAddressDetail());
                exhibitionHallMapper.update(hall);
            }
        }
        // 2. Exhibition 엔티티 저장
        Exhibition exhibition = new Exhibition();
        exhibition.setExhibitionName(dto.getExhibitionName());
        exhibition.setMainImageUrl(dto.getMainImageUrl());
        exhibition.setHallId(hallId);
        if(dto.getLatitude() != null && !dto.getLatitude().isEmpty()) exhibition.setLatitude(new BigDecimal(dto.getLatitude()));
        if(dto.getLongitude() != null && !dto.getLongitude().isEmpty()) exhibition.setLongitude(new BigDecimal(dto.getLongitude()));
        exhibition.setAdmissionFee(dto.getAdmissionFee());
        exhibition.setOpeningHours(dto.getOpeningHours());
        exhibition.setDescription(dto.getDescription());
        exhibition.setType(dto.getType());
        exhibitionMapper.insert(exhibition);
        Long newExhibitionId = exhibition.getId();
        // 3. [수정] 'mapping' 테이블에 학년/과목 정보 저장 (로직 활성화)
        Long gradeId = mappingMapper.getGradeIdByName(dto.getGrade());
        Long subCategoryId = mappingMapper.getSubCategoryIdBySubjectName(dto.getSubject());
        if (gradeId != null) {
            mappingMapper.insertExhibitionGradeMapping(newExhibitionId, gradeId);
        }
        if (subCategoryId != null) {
            mappingMapper.insertExhibitionCurriculumMapping(newExhibitionId, subCategoryId);
        }
    }
    @Transactional
    public void updateExhibition(Long id, ExhibitionAdminRequestDto dto, String newImageUrl) {
        Exhibition exhibition = exhibitionMapper.findById(id);
        if (exhibition == null) {
            throw new RuntimeException("Exhibition not found with id: " + id);
        }
        // 1. 전시관(Hall) 처리 로직 (기존과 동일)
        ExhibitionHall hall = exhibitionHallMapper.findByName(dto.getHallName());
        Long hallId;
        if (hall == null) {
            ExhibitionHall newHall = new ExhibitionHall();
            newHall.setHallName(dto.getHallName());
            newHall.setAddressDetail(dto.getAddressDetail());
            exhibitionHallMapper.insert(newHall);
            hallId = newHall.getId();
        } else {
            hallId = hall.getId();
            if (dto.getAddressDetail() != null && !dto.getAddressDetail().equals(hall.getAddressDetail())) {
                hall.setAddressDetail(dto.getAddressDetail());
                exhibitionHallMapper.update(hall);
            }
        }
        // 2. Exhibition 엔티티 업데이트
        exhibition.setExhibitionName(dto.getExhibitionName());
        exhibition.setHallId(hallId);
        if(dto.getLatitude() != null && !dto.getLatitude().isEmpty()) exhibition.setLatitude(new BigDecimal(dto.getLatitude()));
        if(dto.getLongitude() != null && !dto.getLongitude().isEmpty()) exhibition.setLongitude(new BigDecimal(dto.getLongitude()));
        exhibition.setDescription(dto.getDescription());
        if (newImageUrl != null) {
            exhibition.setMainImageUrl(newImageUrl);
        }
        exhibitionMapper.update(exhibition);
        // 3. [수정] 'mapping' 정보 업데이트 (로직 활성화)
        // 3-1. 기존 매핑 정보 삭제
        mappingMapper.deleteExhibitionGradeMapping(id);
        mappingMapper.deleteExhibitionCurriculumMapping(id);
        // 3-2. 새 매핑 정보 조회 및 저장
        Long gradeId = mappingMapper.getGradeIdByName(dto.getGrade());
        Long subCategoryId = mappingMapper.getSubCategoryIdBySubjectName(dto.getSubject());
        if (gradeId != null) {
            mappingMapper.insertExhibitionGradeMapping(id, gradeId);
        }
        if (subCategoryId != null) {
            mappingMapper.insertExhibitionCurriculumMapping(id, subCategoryId);
        }
    }
    @Transactional
    public void deleteExhibition(Long id) {
        // [수정] 매핑 정보 삭제
        mappingMapper.deleteExhibitionGradeMapping(id);
        mappingMapper.deleteExhibitionCurriculumMapping(id);
        exhibitionMapper.deleteById(id);
    }
}
