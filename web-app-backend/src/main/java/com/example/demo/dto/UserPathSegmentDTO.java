package com.example.demo.dto;

import lombok.Data;
// Lombok @Data 어노테이션은 Getter, Setter, toString 등을 자동 생성합니다.

@Data
public class UserPathSegmentDTO {
    // 1. 경로 핵심 데이터
    private int segmentCount;   // 이동 빈도수 (선 두께 결정용)
    private Long startPlaceId;
    private Long endPlaceId;
    private String pathGeoJson; // 두 장소의 좌표를 이은 GeoJSON LineString (경로 선 그리기용)

    // 2. 실무적 활용 데이터 (장소 이름 및 좌표)

    // 시작 장소 정보 (마커/팝업/통계 패널에 사용)
    private String startPlaceName; // 마커 팝업 및 통계 패널에 표시할 실제 장소 이름
    private double startLat;       // 마커 생성 및 지도 경계 설정용 (위도)
    private double startLng;       // 마커 생성 및 지도 경계 설정용 (경도)

    // 도착 장소 정보 (마커/팝업/통계 패널에 사용)
    private String endPlaceName;   // 마커 팝업 및 통계 패널에 표시할 실제 장소 이름
    private double endLat;         // 마커 생성 및 지도 경계 설정용 (위도)
    private double endLng;         // 마커 생성 및 지도 경계 설정용 (경도)
}