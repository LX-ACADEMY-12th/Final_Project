package com.example.demo.service;

import com.example.demo.dto.AdminPageResponseDTO;
import com.example.demo.dto.ExhibitionDTO;
import com.example.demo.dto.PlaceResultDTO;
import com.example.demo.mapper.ExhibitionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 전시 '목록' 관련 비즈니스 로직을 처리하는 서비스
 */
@Service
public class ExhibitionService {

    // 1. ExhibitionMapper (목록용 매퍼)를 주입받습니다.
    // (ExhibitionDetailMapper 아님)
    @Autowired
    private ExhibitionMapper exhibitionMapper;

    /**
     * (카테고리별) 모든 전시 목록을 조회합니다.
     * Controller로부터 category 값을 받아서 Mapper로 그대로 전달합니다.
     */
    public List<ExhibitionDTO> findAllExhibitions(String category) {
        
        // 2. Mapper의 메서드를 호출해서 DB 결과를 받습니다.
        return exhibitionMapper.findAllExhibitions(category);
    }

    // --- [페이지네이션용 새 메서드 추가] ---
    /**
     * [새 메서드] 관리자 페이지의 통합 콘텐츠 목록을 페이징하여 조회합니다.
     * @param page 요청 페이지 번호 (1부터 시작)
     * @param size 페이지당 항목 수
     * @return AdminPageResponseDTO (목록 + 총 개수)
     */
    public AdminPageResponseDTO findPaginatedAdminContent(int page, int size) {
        // 1. DB용 파라미터 계산 (OFFSET)
        int offset = (page - 1) * size;
        Map<String, Object> params = new HashMap<>();
        params.put("limit", size);
        params.put("offset", offset);
        // 2. DB에서 현재 페이지 목록 조회
        List<PlaceResultDTO> content = exhibitionMapper.findCombinedAdminContent(params);
        // 3. DB에서 전체 항목 개수 조회
        int totalElements = exhibitionMapper.countCombinedAdminContent();
        // 4. DTO로 포장하여 반환
        return new AdminPageResponseDTO(content, totalElements);
    }
}