package com.example.demo.controller;

import com.example.demo.dto.AdminPageResponseDTO; // [추가]
import com.example.demo.dto.ExhibitionAdminRequestDto;
import com.example.demo.dto.PlaceResultDTO; 
import com.example.demo.dto.SciencePlaceAdminRequestDto;
import com.example.demo.service.ExhibitionDetailService;
import com.example.demo.service.ExhibitionService; // [추가]
import com.example.demo.service.FileUploadService;
import com.example.demo.service.PlaceDetailService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList; 
import java.util.Comparator; 
import java.util.List; 
import java.util.Map;

@RestController
@RequestMapping("/api/admin/contents") 
@RequiredArgsConstructor 
public class AdminContentController {

    // [수정] ExhibitionService도 주입받습니다.
    private final ExhibitionService exhibitionListService; // (수정) '목록' 서비스
    
    // (기존) '상세/쓰기' 서비스
    private final ExhibitionDetailService exhibitionService; 
    private final PlaceDetailService placeService;         
    private final FileUploadService fileUploadService;
    private final ObjectMapper objectMapper; 

    /**
     * (Read) 모든 컨텐츠 목록 조회 [페이지네이션 적용]
     * @param page 요청 페이지 (기본 1)
     * @param size 페이지당 개수 (기본 10)
     */
    @GetMapping
    public ResponseEntity<?> getAllContents(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        try {
            // [수정] 페이징 서비스를 호출합니다.
            AdminPageResponseDTO response = exhibitionListService.findPaginatedAdminContent(page, size);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("목록 조회 실패: " + e.getMessage());
        }
    }

    /**
     * (Create) 신규 컨텐츠 등록 (기존 코드 유지)
     */
    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<?> createContent(
            @RequestPart("placeData") String placeDataJson,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        
        try {
            String imageUrl = fileUploadService.uploadImage(file);
            Map<String, Object> dataMap = objectMapper.readValue(placeDataJson, Map.class);
            String type = (String) dataMap.get("type");

            if ("PERMANENT".equals(type)) { 
                ExhibitionAdminRequestDto dto = objectMapper.convertValue(dataMap, ExhibitionAdminRequestDto.class);
                dto.setMainImageUrl(imageUrl);
                exhibitionService.createExhibition(dto); 
                
            } else if ("PLACE".equals(type)) { 
                SciencePlaceAdminRequestDto dto = objectMapper.convertValue(dataMap, SciencePlaceAdminRequestDto.class);
                dto.setMainImageUrl(imageUrl);
                placeService.createSciencePlace(dto); 
            } else {
                return ResponseEntity.badRequest().body("알 수 없는 타입입니다.");
            }
            
            return ResponseEntity.ok().build();

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("등록 실패: " + e.getMessage());
        }
    }

    /**
     * (Update) 컨텐츠 수정 (기존 코드 유지)
     */
    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<?> updateContent(
            @PathVariable("id") Long id,
            @RequestPart("placeData") String placeDataJson,
            @RequestPart(value = "file", required = false) MultipartFile file) {

        try {
            String newImageUrl = fileUploadService.uploadImage(file);
            Map<String, Object> dataMap = objectMapper.readValue(placeDataJson, Map.class);
            String type = (String) dataMap.get("type");

            if ("PERMANENT".equals(type)) {
                ExhibitionAdminRequestDto dto = objectMapper.convertValue(dataMap, ExhibitionAdminRequestDto.class);
                exhibitionService.updateExhibition(id, dto, newImageUrl); 
                
            } else if ("PLACE".equals(type)) {
                SciencePlaceAdminRequestDto dto = objectMapper.convertValue(dataMap, SciencePlaceAdminRequestDto.class);
                placeService.updateSciencePlace(id, dto, newImageUrl); 
            } else {
                return ResponseEntity.badRequest().body("알 수 없는 타입입니다.");
            }
            
            return ResponseEntity.ok().build();

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("수정 실패: " + e.getMessage());
        }
    }

    /**
     * (Delete) 컨텐츠 삭제 (기존 코드 유지)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteContent(@PathVariable("id") Long id) {
        try {
            exhibitionService.deleteExhibition(id);
        } catch (Exception e) {
            // (무시)
        }
        try {
            placeService.deleteSciencePlace(id);
        } catch (Exception e) {
            // (무시)
        }

        return ResponseEntity.ok().build();
    }
}