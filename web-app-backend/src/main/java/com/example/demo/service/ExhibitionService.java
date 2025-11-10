package com.example.demo.service;

import com.example.demo.dto.*;
import com.example.demo.mapper.ExhibitionMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 전시 '목록' 관련 비즈니스 로직을 처리하는 서비스
 */
@Slf4j
@Service
public class ExhibitionService {

    // 1. ExhibitionMapper (목록용 매퍼)를 주입받습니다.
    // (ExhibitionDetailMapper 아님)
    @Autowired
    private ExhibitionMapper exhibitionMapper;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * (카테고리별) 모든 전시 목록을 조회합니다.
     * Controller로부터 category 값을 받아서 Mapper로 그대로 전달합니다.
     */
    public List<ExhibitionDTO> findAllExhibitions(String category) {
        
        // 2. Mapper의 메서드를 호출해서 DB 결과를 받습니다.
        return exhibitionMapper.findAllExhibitions(category);
    }

    /**
     * API 1: 전시관 상세 정보 조회 (숫자 ID 사용)
     */
    public HallDetailDTO getHallDetail(Long hallId) {
        // [로그 추가] 1. 서비스 진입 및 파라미터 확인
        log.info("getHallDetail() 호출 - hallId: {}", hallId);

        // 1. DB에서 String 'floors'를 포함한 Result 객체를 받음
        HallDetailResultDTO result = exhibitionMapper.findHallDetailById(hallId);

        if (result == null) {
            // [로그 추가] 2. DB 조회 실패 (결과 없음)
            log.warn("getHallDetail() - DB에서 hallId {} 에 해당하는 정보를 찾을 수 없습니다.", hallId);
            throw new RuntimeException("전시관 정보를 찾을 수 없습니다. ID: " + hallId);
        }

        // [로그 추가] 3. DB 조회 성공 (간단한 결과 로깅)
        log.info("getHallDetail() - DB 조회 성공. hallId: {}, scienceCenterId: {}", result.getHallId(), result.getScienceCenterId());

        // 2. API용 DTO로 변환
        HallDetailDTO dto = new HallDetailDTO();
        dto.setHallId(result.getHallId());
        dto.setScienceCenterId(result.getScienceCenterId());
        dto.setExhibitionLocation(result.getExhibitionLocation());
        dto.setLat(result.getLat());
        dto.setLng(result.getLng());
        dto.setOperationalPeriod(result.getOperationPeriod());
        dto.setOperationHours(result.getOperationHours());
        dto.setEntranceFee(result.getEntranceFee());

        // 3. [핵심] JSON 문자열을 List<String>으로 파싱
        dto.setFloors(parseFloors(result.getFloors()));

        return dto;
    }

    /**
     * API 2: 특정 과학관 이름으로 모든 전시관 목록 조회
     */
    public List<HallSimpleDTO> getHallsByScienceCenterName(String scienceCenterName) {
        // [로그 추가] 1. 서비스 진입 및 파라미터 확인
        log.info("getHallsByScienceCenterName() 호출 - scienceCenterName: {}", scienceCenterName);
        // 1. DB에서 Result 리스트를 받음
        List<HallSimpleResultDTO> results = exhibitionMapper.findHallsByScienceCenterName(scienceCenterName);

        if (results.isEmpty()) {
            // [로그 추가] 2. DB 조회 결과가 없음 (오류는 아님)
            log.info("getHallsByScienceCenterName() - '{}'에 해당하는 전시관 목록이 없습니다.", scienceCenterName);
            return Collections.emptyList(); // 빈 리스트 반환
        }

        // [로그 추가] 3. DB 조회 성공 (결과 개수 로깅)
        log.info("getHallsByScienceCenterName() - '{}' 이름으로 {}개의 전시관을 찾았습니다.", scienceCenterName, results.size());

        // 2. [핵심] Stream을 사용해 List<HallSimpleResultDTO>를 List<HallSimpleDTO>로 변환
        return results.stream().map(result -> {
            HallSimpleDTO dto = new HallSimpleDTO();
            dto.setKey(result.getKey());
            dto.setName(result.getName());
            dto.setLocationDescription(result.getLocationDescription());
            // 3. 각 항목의 JSON 문자열을 파싱
            dto.setFloors(parseFloors(result.getFloors()));
            return dto;
        }).collect(Collectors.toList());
    }

    /**
     * JSON 문자열(예: "[\"1F\", \"2F\"]")을 List<String>으로 변환하는 헬퍼 메서드
     */
    private List<String> parseFloors(String floorsJson) {
        if (floorsJson == null || floorsJson.isEmpty() || floorsJson.equals("[]")) {
            return new ArrayList<>();
        }

        try {
            // ObjectMapper를 사용해 JSON 배열 문자열을 List<String>으로 변환
            return objectMapper.readValue(floorsJson, new TypeReference<List<String>>() {});
        } catch (IOException e) {
            // 파싱 실패 시 로그를 남기고 빈 리스트를 반환
            System.err.println("Failed to parse floors JSON: " + floorsJson + e.getMessage());
            return new ArrayList<>();
        }
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