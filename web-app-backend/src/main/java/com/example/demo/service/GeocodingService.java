package com.example.demo.service;

import com.example.demo.dto.GeocodeResponseDTO;
import com.example.demo.dto.KakaoAddressResponseDTO;
import com.example.demo.dto.KakaoDocumentDTO;
import com.example.demo.dto.KakaoCoord2AddressDTO; // [신규]
import com.example.demo.dto.KakaoCoord2AddressResponseDTO; // [신규]

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor // [수정] final 필드 주입을 위해
public class GeocodingService {

    // [신규] RestTemplate 주입
    private final RestTemplate restTemplate;

    // [신규] application.properties에서 API 키 주입
    @Value("${kakao.api.key}")
    private String kakaoApiKey;

    // [신규] 카카오 API 엔드포인트
    private static final String KAKAO_KEYWORD_SEARCH_URL = "https://dapi.kakao.com/v2/local/search/keyword.json";
    // [신규] 좌표 -> 주소 변환 API URL
    private static final String KAKAO_COORD_TO_ADDRESS_URL = "https://dapi.kakao.com/v2/local/geo/coord2address.json";

    public GeocodeResponseDTO getCoordinates(String address) {
        if (address == null || address.trim().isEmpty()) {
            return null;
        }

        System.out.println("[Geocoding] API 호출 (키워드): " + address); // '키워드'로 로그 수정

        try {
            // 1. HTTP 헤더 설정 (인증)
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "KakaoAK " + kakaoApiKey);

            // 2. HTTP 엔티티 생성 (헤더 포함)
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // 3. URI 빌더 생성 (쿼리 파라미터)
            UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(KAKAO_KEYWORD_SEARCH_URL)
                    .queryParam("query", address);

            // 4. API 호출 및 응답 받기
            ResponseEntity<KakaoAddressResponseDTO> response = restTemplate.exchange(
                    uriBuilder.toUriString(),
                    HttpMethod.GET,
                    entity,
                    KakaoAddressResponseDTO.class // 응답을 이 DTO로 파싱
            );

            // 5. 응답 본문에서 좌표 추출
            if (response.getBody() != null && response.getBody().getDocuments() != null) {
                List<KakaoDocumentDTO> documents = response.getBody().getDocuments();

                if (!documents.isEmpty()) {
                    // [중요] 첫 번째 검색 결과를 사용
                    KakaoDocumentDTO firstDocument = documents.get(0);

                    BigDecimal latitude = firstDocument.getLatitude();
                    BigDecimal longitude = firstDocument.getLongitude();

                    System.out.println("[Geocoding] 성공: " + latitude + ", " + longitude);
                    return new GeocodeResponseDTO(latitude, longitude);
                }
            }

            // 6. 검색 결과가 없는 경우
            System.out.println("[Geocoding] 결과 없음: " + address);
            return null;

        } catch (HttpClientErrorException e) {
            // API 키 오류, 쿼터 초과 등 HTTP 에러
            System.err.println("[Geocoding] API 호출 에러: " + e.getMessage());
            return null;
        } catch (Exception e) {
            // 기타 네트워크 에러 등
            System.err.println("[Geocoding] 알 수 없는 에러: " + e.getMessage());
            return null;
        }
    }

    /**
     * [신규] 좌표 -> 주소 변환 (Reverse Geocoding)
     */
    public String getAddressFromCoordinates(String longitude, String latitude) {
        if (longitude == null || latitude == null) {
            return null;
        }

        System.out.println("[Reverse Geocoding] API 호출: " + latitude + ", " + longitude);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "KakaoAK " + kakaoApiKey);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(KAKAO_COORD_TO_ADDRESS_URL)
                    .queryParam("x", longitude) // 카카오는 경도(x), 위도(y) 순서
                    .queryParam("y", latitude);

            ResponseEntity<KakaoCoord2AddressResponseDTO> response = restTemplate.exchange(
                    uriBuilder.toUriString(),
                    HttpMethod.GET,
                    entity,
                    KakaoCoord2AddressResponseDTO.class
            );

            if (response.getBody() != null && response.getBody().getDocuments() != null) {
                List<KakaoCoord2AddressDTO> documents = response.getBody().getDocuments();
                if (!documents.isEmpty()) {
                    String addressName = documents.get(0).getAddressName();
                    System.out.println("[Reverse Geocoding] 성공: " + addressName);
                    return addressName;
                }
            }

            System.out.println("[Reverse Geocoding] 결과 없음");
            return null;

        } catch (Exception e) {
            System.err.println("[Reverse Geocoding] API 호출 에러: " + e.getMessage());
            return null;
        }
    }
}
