package com.example.demo.controller;

import com.example.demo.dto.*;
import com.example.demo.service.AdminContentService;
import com.example.demo.service.GeocodingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/content")
@RequiredArgsConstructor
public class AdminContentController {

    private final AdminContentService adminContentService;
    private final GeocodingService geocodingService;
    private final ObjectMapper objectMapper;

    // --- JSON 문자열을 DTO로 변환하는 헬퍼 ---
    private <T> T getDtoFromString(String dtoString, Class<T> clazz) {
        try {
            return objectMapper.readValue(dtoString, clazz);
        } catch (IOException e) {
            throw new IllegalArgumentException("Invalid DTO format: " + e.getMessage(), e);
        }
    }

    // --- Geocoding ---
    @PostMapping("/geocode")
    public ResponseEntity<GeocodeResponseDTO> geocodeAddress(@RequestBody Map<String, String> payload) {
        String address = payload.get("address");
        return ResponseEntity.ok(geocodingService.getCoordinates(address));
    }

    // ========== Exhibition Hall (전시관) ==========
    @GetMapping("/halls")
    public ResponseEntity<List<AdminHallListDTO>> getHalls() {
        return ResponseEntity.ok(adminContentService.getHallList());
    }
    @GetMapping("/halls/{id}")
    public ResponseEntity<AdminHallDetailDTO> getHallById(@PathVariable Long id) {
        return ResponseEntity.ok(adminContentService.getHallById(id));
    }
    @PostMapping("/halls")
    public ResponseEntity<AdminHallDetailDTO> createHall(
            @RequestPart("dto") String dtoString,
            @RequestPart(value = "mainImage", required = false) MultipartFile mainImage) {
        AdminHallDetailDTO dto = getDtoFromString(dtoString, AdminHallDetailDTO.class);
        return ResponseEntity.status(HttpStatus.CREATED).body(adminContentService.createExhibitionHall(dto, mainImage));
    }
    @PutMapping("/halls/{id}")
    public ResponseEntity<AdminHallDetailDTO> updateHall(
            @PathVariable Long id,
            @RequestPart("dto") String dtoString,
            @RequestPart(value = "mainImage", required = false) MultipartFile mainImage) {
        AdminHallDetailDTO dto = getDtoFromString(dtoString, AdminHallDetailDTO.class);
        return ResponseEntity.ok(adminContentService.updateExhibitionHall(id, dto, mainImage));
    }
    @DeleteMapping("/halls/{id}")
    public ResponseEntity<Void> deleteHall(@PathVariable Long id) {
        adminContentService.deleteHall(id);
        return ResponseEntity.noContent().build();
    }

    // ========== Exhibition (전시) ==========
    @GetMapping("/exhibitions")
    public ResponseEntity<List<AdminExhibitionListDTO>> getExhibitions(
            @RequestParam(value = "hallId", required = false) Long hallId) {
        return ResponseEntity.ok(adminContentService.getExhibitionList(hallId));
    }
    @GetMapping("/exhibitions/{id}")
    public ResponseEntity<AdminExhibitionDetailDTO> getExhibitionById(@PathVariable Long id) {
        return ResponseEntity.ok(adminContentService.getExhibitionById(id));
    }
    @PostMapping("/exhibitions")
    public ResponseEntity<AdminExhibitionDetailDTO> createExhibition(
            @RequestPart("dto") String dtoString,
            @RequestPart(value = "mainImage", required = false) MultipartFile mainImage) {
        AdminExhibitionDetailDTO dto = getDtoFromString(dtoString, AdminExhibitionDetailDTO.class);
        return ResponseEntity.status(HttpStatus.CREATED).body(adminContentService.createExhibition(dto, mainImage));
    }
    @PutMapping("/exhibitions/{id}")
    public ResponseEntity<AdminExhibitionDetailDTO> updateExhibition(
            @PathVariable Long id,
            @RequestPart("dto") String dtoString,
            @RequestPart(value = "mainImage", required = false) MultipartFile mainImage) {
        AdminExhibitionDetailDTO dto = getDtoFromString(dtoString, AdminExhibitionDetailDTO.class);
        return ResponseEntity.ok(adminContentService.updateExhibition(id, dto, mainImage));
    }
    @DeleteMapping("/exhibitions/{id}")
    public ResponseEntity<Void> deleteExhibition(@PathVariable Long id) {
        adminContentService.deleteExhibition(id);
        return ResponseEntity.noContent().build();
    }

    // ========== Science Place (체험 장소) ==========
    @GetMapping("/places")
    public ResponseEntity<List<AdminPlaceListDTO>> getPlaces() {
        return ResponseEntity.ok(adminContentService.getPlaceList());
    }
    @GetMapping("/places/{id}")
    public ResponseEntity<AdminPlaceDetailDTO> getPlaceById(@PathVariable Long id) {
        return ResponseEntity.ok(adminContentService.getPlaceById(id));
    }
    @PostMapping("/places")
    public ResponseEntity<AdminPlaceDetailDTO> createPlace(
            @RequestPart("dto") String dtoString,
            @RequestPart(value = "mainImage", required = false) MultipartFile mainImage) {
        AdminPlaceDetailDTO dto = getDtoFromString(dtoString, AdminPlaceDetailDTO.class);
        return ResponseEntity.status(HttpStatus.CREATED).body(adminContentService.createSciencePlace(dto, mainImage));
    }
    @PutMapping("/places/{id}")
    public ResponseEntity<AdminPlaceDetailDTO> updatePlace(
            @PathVariable Long id,
            @RequestPart("dto") String dtoString,
            @RequestPart(value = "mainImage", required = false) MultipartFile mainImage) {
        AdminPlaceDetailDTO dto = getDtoFromString(dtoString, AdminPlaceDetailDTO.class);
        return ResponseEntity.ok(adminContentService.updateSciencePlace(id, dto, mainImage));
    }
    @DeleteMapping("/places/{id}")
    public ResponseEntity<Void> deletePlace(@PathVariable Long id) {
        adminContentService.deletePlace(id);
        return ResponseEntity.noContent().build();
    }

    // ========== [신규] Modal 공통 데이터 API ==========

    @GetMapping("/common/halls")
    public ResponseEntity<List<AdminSimpleHallDTO>> getCommonHalls() {
        return ResponseEntity.ok(adminContentService.getSimpleHallList());
    }

    @GetMapping("/common/grades")
    public ResponseEntity<List<AdminGradeCategoryDTO>> getCommonGrades() {
        return ResponseEntity.ok(adminContentService.getGradeCategoryList());
    }

    @GetMapping("/common/subcategories")
    public ResponseEntity<List<AdminSubCategoryDetailDTO>> getCommonSubCategories() {
        return ResponseEntity.ok(adminContentService.getSubCategoryDetailList());
    }


    /**
     * [신규] 좌표 -> 주소 변환 API
     */
    @PostMapping("/reverse-geocode")
    public ResponseEntity<Map<String, String>> reverseGeocodeAddress(@RequestBody GeocodeResponseDTO payload) {
        String address = geocodingService.getAddressFromCoordinates(
                payload.getLongitude().toPlainString(),
                payload.getLatitude().toPlainString()
        );

        if (address != null) {
            return ResponseEntity.ok(Collections.singletonMap("address", address));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
}