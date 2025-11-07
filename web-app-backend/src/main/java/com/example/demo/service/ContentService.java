package com.example.demo.service;

import com.example.demo.dto.ContentResultDTO;
import com.example.demo.dto.ContentSearchDTO;
import com.example.demo.mapper.ContentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ContentService {

    @Autowired
    private ContentMapper contentMapper; // Mapper Interface 주입

    public List<ContentResultDTO> findPlaces(ContentSearchDTO dto) {
        // 검색 요청값
        String targetGrade = dto.getGrade() != null ? dto.getGrade() : null;
        dto.setGrade(targetGrade);
        
        // 검색 결과 담을 리스트
        List<ContentResultDTO> results;

        // 검색 요청에서 itemType 가져오기 -> 
        // boolean isExhibition = "전시".equals(dto.getItemType());

        // 검색 요청에서 검색유형 가져오기
        switch (dto.getSearchType()) {
            // 반경 검색일 경우에            
            case "radius":
                // 위도, 경도, 반경 값이 있는지 확인
                if (dto.getLat() == null || dto.getLng() == null || dto.getRadius() == null) {
                    throw new IllegalArgumentException("반경 검색 시 위도, 경도, 반경 값은 필수입니다.");
                }
                // PostGIS 사용을 위한 거리 변환 (km -> meter)
                double radiusInMeters = dto.getRadius() * 1000.0;

                // 통합 메소드 호출
                results = contentMapper.findContentByRadius(dto.getSubject(), dto.getGrade(), dto.getLat(), dto.getLng(), radiusInMeters);
                break;

            // 지역 검색일 경우에
            case "region":
                // 지역 값이 있는지 확인
                if (dto.getRegion() == null || dto.getRegion().trim().isEmpty()) {
                    throw new IllegalArgumentException("지역 검색 시 지역명은 필수입니다.");
                }
                String regionPattern = "%" + dto.getRegion().trim() + "%"; // LIKE 검색 위한 패턴

                // 통합 메소드 호출
                results = contentMapper.findContentByRegion(dto.getSubject(), dto.getGrade(), regionPattern);
                break;

            // 일반 검색일 경우에
            case "all":
            default:
                results = contentMapper.findContentByFilter(dto.getSubject(), dto.getGrade());
                break;
        }
        return results;
    }

}