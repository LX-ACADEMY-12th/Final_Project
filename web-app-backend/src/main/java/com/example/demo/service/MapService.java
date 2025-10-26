package com.example.demo.service;

import com.example.demo.dto.PlaceResultDTO;
import com.example.demo.dto.PlaceSearchDTO;
import com.example.demo.mapper.PlaceMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MapService {

    @Autowired
    private PlaceMapper placeMapper; // Mapper Interface 주입

    public List<PlaceResultDTO> findPlaces(PlaceSearchDTO dto) {
        String targetGrade = dto.getGrade() != null ? dto.getGrade().replace("초등 ", "") : null;
        dto.setGrade(targetGrade); // "3학년" 형식으로 변경

        List<PlaceResultDTO> results;

        boolean isExhibition = "전시".equals(dto.getItemType());

        switch (dto.getSearchType()) {
            case "radius":
                // 위도, 경도, 반경 값이 있는지 확인
                if (dto.getLat() == null || dto.getLng() == null || dto.getRadius() == null) {
                    throw new IllegalArgumentException("반경 검색 시 위도, 경도, 반경 값은 필수입니다.");
                }
                // PostGIS 사용을 위한 거리 변환 (km -> meter)
                double radiusInMeters = dto.getRadius() * 1000.0;

                // Mapper 메소드 호출 (PostGIS 함수 사용)
                if (isExhibition) {
                    results = placeMapper.findExhibitionsByRadius(dto.getSubject(), dto.getGrade(), dto.getLat(), dto.getLng(), dto.getRadius());
                } else {
                    results = placeMapper.findPlacesByRadius(dto.getSubject(), dto.getGrade(), dto.getLat(), dto.getLng(), dto.getRadius());
                }
                break;
            case "region":
                // 지역 값이 있는지 확인
                if (dto.getRegion() == null || dto.getRegion().trim().isEmpty()) {
                    throw new IllegalArgumentException("지역 검색 시 지역명은 필수입니다.");
                }
                String regionPattern = "%" + dto.getRegion().trim() + "%"; // LIKE 검색 위한 패턴

                // Mapper 메소드 호출 (LIKE 검색)
                if (isExhibition) {
                    results = placeMapper.findExhibitionsByRegion(dto.getSubject(), dto.getGrade(), regionPattern);
                } else {
                    results = placeMapper.findPlacesByRegion(dto.getSubject(), dto.getGrade(), regionPattern);
                }
                break;
            case "all":
            default:
                // Mapper 메소드 호출 (기본 필터)
                if (isExhibition) {
                    results = placeMapper.findExhibitionsByFilter(dto.getSubject(), dto.getGrade());
                } else {
                    results = placeMapper.findPlacesByFilter(dto.getSubject(), dto.getGrade());
                }
                break;
        }

        // Hashtag 처리 등 추가 가공 로직 (필요시)
        // 예: DB에는 콤마로 구분된 문자열 -> List<String> 변환
        // results.forEach(item -> item.setHashtags(parseHashtags(item.getRawHashtags())));

        return results;
    }

    // private List<String> parseHashtags(String rawHashtags) { ... }
}