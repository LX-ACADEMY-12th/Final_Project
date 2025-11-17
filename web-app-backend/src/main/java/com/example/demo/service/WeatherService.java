package com.example.demo.service;

import com.example.demo.util.GeoTransUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
public class WeatherService {

    @Value("${weather.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public WeatherService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public String getCurrentWeatherByLatLon(double lat, double lon) {
        int[] grid = GeoTransUtils.convertToGrid(lat, lon);
        log.info("[WeatherService] 위도={}, 경도={} → 격자좌표 nx={}, ny={}",
                lat, lon, grid[0], grid[1]);
        return getCurrentWeather(grid[0], grid[1]);
    }

    public String getCurrentWeather(int nx, int ny) {
        try {
            LocalDateTime now = LocalDateTime.now();

            LocalDateTime baseDateTime;
            int currentMinute = now.getMinute();

            if (currentMinute < 45) {
                baseDateTime = now.minusHours(1).withMinute(30).withSecond(0).withNano(0);
            } else {
                baseDateTime = now.withMinute(30).withSecond(0).withNano(0);
            }

            String baseDate = baseDateTime.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String baseTime = baseDateTime.format(DateTimeFormatter.ofPattern("HHmm"));

            // ★ 브라우저에서 작동한 URL과 동일한 형식
            String url = String.format(
                    "https://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getUltraSrtFcst?" +
                            "serviceKey=%s&pageNo=1&numOfRows=60&dataType=JSON&base_date=%s&base_time=%s&nx=%d&ny=%d",
                    apiKey, baseDate, baseTime, nx, ny
            );

            log.info("[WeatherService] 요청 URL: {}", url);

            // URI로 직접 생성하여 호출
            String response = restTemplate.getForObject(URI.create(url), String.class);

            JsonNode root = objectMapper.readTree(response);

            String resultCode = root.path("response").path("header").path("resultCode").asText();
            String resultMsg = root.path("response").path("header").path("resultMsg").asText();

            log.info("[WeatherService] API 응답: {} - {}", resultCode, resultMsg);

            if (!"00".equals(resultCode)) {
                log.error("[WeatherService] API 오류: {}", resultMsg);
                return "날씨 정보 조회 실패";
            }

            JsonNode items = root.path("response").path("body").path("items").path("item");

            if (items.isEmpty()) {
                log.warn("[WeatherService] 날씨 데이터 없음");
                return "날씨 정보 없음";
            }

            String targetTime = now.format(DateTimeFormatter.ofPattern("HHmm"));

            double temperature = 0.0;
            int humidity = 0;
            String skyCondition = "";
            String precipitation = "없음";

            for (JsonNode item : items) {
                String fcstTime = item.path("fcstTime").asText();

                if (fcstTime.compareTo(targetTime) >= 0) {
                    String category = item.path("category").asText();
                    String value = item.path("fcstValue").asText();

                    switch (category) {
                        case "T1H":
                            temperature = Double.parseDouble(value);
                            break;
                        case "REH":
                            humidity = Integer.parseInt(value);
                            break;
                        case "SKY":
                            skyCondition = getSkyCondition(value);
                            break;
                        case "PTY":
                            precipitation = getPrecipitationType(value);
                            break;
                    }
                }
            }

            String result = String.format("%s, 기온 %.1f℃, 습도 %d%%",
                    skyCondition.isEmpty() ? "정보없음" : skyCondition, temperature, humidity);

            if (!precipitation.equals("없음")) {
                result += ", 강수 " + precipitation;
            }

            log.info("[WeatherService] 날씨: {}", result);
            return result;

        } catch (Exception e) {
            log.error("[WeatherService] 오류: {}", e.getMessage());
            return "날씨 정보 조회 실패";
        }
    }

    private String getSkyCondition(String code) {
        return switch (code) {
            case "1" -> "맑음";
            case "3" -> "구름많음";
            case "4" -> "흐림";
            default -> "알 수 없음";
        };
    }

    private String getPrecipitationType(String code) {
        return switch (code) {
            case "0" -> "없음";
            case "1" -> "비";
            case "2" -> "비/눈";
            case "3" -> "눈";
            case "4" -> "소나기";
            default -> "알 수 없음";
        };
    }

    public String getWeatherRecommendation(String weatherInfo) {
        if (weatherInfo.contains("비") || weatherInfo.contains("눈")) {
            return "날씨가 좋지 않아 실내 활동 권장";
        } else if (weatherInfo.contains("맑음")) {
            return "날씨 좋음, 야외 활동 추천!";
        } else if (weatherInfo.contains("구름많음")) {
            return "실내외 활동 모두 가능";
        } else {
            return "실내 활동 조금 더 추천";
        }
    }

    private double extractTemperature(String weatherInfo) {
        try {
            return Double.parseDouble(weatherInfo.split("기온 ")[1].split("℃")[0]);
        } catch (Exception e) {
            return 20.0;
        }
    }
}