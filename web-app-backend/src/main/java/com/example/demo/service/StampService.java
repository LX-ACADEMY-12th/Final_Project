package com.example.demo.service;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.ExhibitionDetailDTO;
import com.example.demo.dto.PlaceDetailDTO;
import com.example.demo.dto.StampRequestDTO;
import com.example.demo.dto.StampResponseDTO;
import com.example.demo.mapper.ExhibitionDetailMapper;
import com.example.demo.mapper.PlaceDetailMapper;
import com.example.demo.mapper.StampMapper;

@Service
public class StampService {

	@Autowired
	private StampMapper stampMapper;
	
	// 전시, 장소 좌표를 가져오는 매퍼
	@Autowired
	private ExhibitionDetailMapper exhibitionDetailMapper;
	
	@Autowired
	private PlaceDetailMapper placeDetailMapper;
	
	@Transactional // 2. 이 어노테이션을 메서드 위에 추가합니다.
	public StampResponseDTO createStamp(StampRequestDTO requestDTO) {
		
		String targetType = requestDTO.getTargetType();
		Long targetId = requestDTO.getTargetId();
		
		// 1. 좌표를 담을 공통 변수 (null로 초기화)
        // (DB에 좌표가 없는 경우를 대비해 Wrapper Type인 Double 사용)
		BigDecimal targetLat = null; 
		BigDecimal targetLon = null;
		
		// 1. targetType에 따라 다른 Mapper 메서드를 호출
		if("exhibition".equals(targetType)) {
			ExhibitionDetailDTO hallLocation = exhibitionDetailMapper.findHallCoordinates(targetId);
		
			if (hallLocation != null) {
                targetLat = hallLocation.getLatitude();
                targetLon = hallLocation.getLongitude();
            }
			
		} else if ("science_place".equals(targetType)) {
			PlaceDetailDTO placeLocation = placeDetailMapper.findPlaceCoordinates(targetId);
            if (placeLocation != null) {
               targetLat = placeLocation.getLatitude();
               targetLon = placeLocation.getLongitude();
           }
            
		} else {
			throw new RuntimeException("알 수 없는 TargetType입니다: " + targetType);
		}
		
		// 3. (2번 항목 완성) 좌표가 DB에 있는지 확인
		if(targetLat == null || targetLon == null) {
			throw new RuntimeException("장소의 좌표 정보를 찾을 수 없습니다.");
		}
		
		// 4. 스탬프 중복 인증 체크
		StampResponseDTO existingStamp = stampMapper.findStampByUserIdAndTarget(requestDTO);
        if (existingStamp != null) {
            throw new RuntimeException("이미 방문한 장소입니다.");
        }
        
     // 5. [핵심] GPS 거리 계산
        double distance = calculateDistance(
            requestDTO.getLatitude(),  // 사용자의 현재 위도
            requestDTO.getLongitude(), // 사용자의 현재 경도
            targetLat,                 // 목표 장소의 위도
            targetLon                  // 목표 장소의 경도
        );
		
		// 6. 인증 처리 (예: 100m 이내만 허용)
        final double ALLOWED_DISTANCE_METERS = 100.0;
        if (distance > ALLOWED_DISTANCE_METERS) {
            throw new RuntimeException("인증 실패: 장소에서 너무 멉니다. (현재 거리: " + (int)distance + "m)");
        }
        
        // 7. 인증 성공 -> DB에 저장할 DTO 준비
        StampResponseDTO stampToSave = new StampResponseDTO();
        stampToSave.setUserId(requestDTO.getUserId());
        stampToSave.setTargetType(requestDTO.getTargetType());
        stampToSave.setTargetId(requestDTO.getTargetId());
        stampToSave.setAuthMethod("GPS"); // GPS로 고정
        stampToSave.setVisitedAt(new java.util.Date()); // 현재 시간

        // 8. DB에 스탬프 저장
        stampMapper.addToVisitStamp(stampToSave);
        
        // 9. recordId가 채워진 DTO 반환
		return stampToSave;
	}
	

	/**
	 * 두 지점 간의 거리(m) 계산 (Haversine formula)
	 * * @param lat1_bd (사용자 위도 - BigDecimal)
	 * @param lon1_bd (사용자 경도 - BigDecimal)
	 * @param lat2_bd (목표 위도 - BigDecimal)
	 * @param lon2_bd (목표 경도 - BigDecimal)
	 * @return double (두 지점 간의 거리 - meter)
	 */
	private double calculateDistance(BigDecimal lat1_bd, BigDecimal lon1_bd, BigDecimal lat2_bd, BigDecimal lon2_bd) {
        
        // 1. [핵심] BigDecimal을 double 타입으로 변환
        double lat1 = lat1_bd.doubleValue();
        double lon1 = lon1_bd.doubleValue();
        double lat2 = lat2_bd.doubleValue();
        double lon2 = lon2_bd.doubleValue();

        // 2. (이하 로직은 double 타입으로 계산)
	    final int R = 6371; // 지구 반지름 (km)
	
	    double latDistance = Math.toRadians(lat2 - lat1);
	    double lonDistance = Math.toRadians(lon2 - lon1);
	
	    double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
	            + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
	            * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
	
	    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
	
	    double distance = R * c; // km 단위
	
	    return distance * 1000; // meter 단위로 변환
	}
	
	
}
