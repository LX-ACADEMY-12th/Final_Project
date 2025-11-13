package com.example.demo.service;

import com.example.demo.dto.*; // DTO 패키지
import com.example.demo.mapper.AdminContentMapper; // Mapper 패키지
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import com.example.demo.dto.AdminSimpleHallDTO;
import com.example.demo.dto.AdminGradeCategoryDTO;
import com.example.demo.dto.AdminSubCategoryDetailDTO;

@Service
@RequiredArgsConstructor
public class AdminContentService {

    private final AdminContentMapper adminContentMapper;
    // [수정] FileStorageService -> FileUploadService
    private final FileUploadService fileUploadService;
    private final GeocodingService geocodingService;

    // --- Utility: 파일 처리 (수정) ---
    private String handleFileUpload(MultipartFile file, String existingImageUrl) {
        if (file != null && !file.isEmpty()) {
            if (existingImageUrl != null) {
                // [수정] fileUploadService.deleteImage 호출
                fileUploadService.deleteImage(existingImageUrl); // 기존 파일 삭제
            }
            // [수정] fileUploadService.uploadImage 호출
            return fileUploadService.uploadImage(file); // 새 파일 저장
        }
        return existingImageUrl; // 새 파일 없으면 기존 URL 유지
    }

    // --- Utility: 지오코딩 (위도/경도 설정) ---
    private void setCoordinates(AdminHallDetailDTO dto, String address) {
        GeocodeResponseDTO coords = geocodingService.getCoordinates(address);
        if (coords != null) {
            dto.setLatitude(coords.getLatitude());
            dto.setLongitude(coords.getLongitude());
        }
    }
    private void setCoordinates(AdminExhibitionDetailDTO dto, String address) {
        GeocodeResponseDTO coords = geocodingService.getCoordinates(address);
        if (coords != null) {
            dto.setLatitude(coords.getLatitude());
            dto.setLongitude(coords.getLongitude());
        }
    }
    private void setCoordinates(AdminPlaceDetailDTO dto, String address) {
        GeocodeResponseDTO coords = geocodingService.getCoordinates(address);
        if (coords != null) {
            dto.setLatitude(coords.getLatitude());
            dto.setLongitude(coords.getLongitude());
        }
    }

    // ========== 1. List (GET) ==========
    public List<AdminHallListDTO> getHallList() {
        return adminContentMapper.findAllHallsForList();
    }
    public List<AdminExhibitionListDTO> getExhibitionList(Long hallId) {
        return adminContentMapper.findAllExhibitionsForList(hallId);
    }
    public List<AdminPlaceListDTO> getPlaceList() {
        return adminContentMapper.findAllPlacesForList();
    }

    // ========== 2. Detail (GET BY ID) ==========
    public AdminHallDetailDTO getHallById(Long id) {
        return adminContentMapper.findHallDetailDTOById(id);
    }
    public AdminExhibitionDetailDTO getExhibitionById(Long id) {
        AdminExhibitionDetailDTO dto = adminContentMapper.findExhibitionDetailDTOById(id);
        if (dto != null) {
            dto.setGradeIds(adminContentMapper.findGradeIdsByExhibitionId(id));
            dto.setSubCategoryIds(adminContentMapper.findSubCategoryIdsByExhibitionId(id));
        }
        return dto;
    }
    public AdminPlaceDetailDTO getPlaceById(Long id) {
        AdminPlaceDetailDTO dto = adminContentMapper.findPlaceDetailDTOById(id);
        if (dto != null) {
            dto.setGradeIds(adminContentMapper.findGradeIdsByPlaceId(id));
            dto.setSubCategoryIds(adminContentMapper.findSubCategoryIdsByPlaceId(id));
        }
        return dto;
    }

    // ========== 3. Create (POST) ==========
    @Transactional
    public AdminHallDetailDTO createExhibitionHall(AdminHallDetailDTO hallDTO, MultipartFile file) {
        hallDTO.setMainImageUrl(handleFileUpload(file, null));
        setCoordinates(hallDTO, hallDTO.getAddressDetail());
        adminContentMapper.insertExhibitionHall(hallDTO);
        return hallDTO;
    }
    @Transactional
    public AdminExhibitionDetailDTO createExhibition(AdminExhibitionDetailDTO exhibitionDTO, MultipartFile file) {
        exhibitionDTO.setMainImageUrl(handleFileUpload(file, null));
        setCoordinates(exhibitionDTO, exhibitionDTO.getAddressDetail());
        adminContentMapper.insertExhibition(exhibitionDTO);
        Long id = exhibitionDTO.getExhibitionId();

        if (exhibitionDTO.getGradeIds() != null && !exhibitionDTO.getGradeIds().isEmpty()) {
            adminContentMapper.insertExhibitionGradeMappings(id, exhibitionDTO.getGradeIds());
        }
        if (exhibitionDTO.getSubCategoryIds() != null && !exhibitionDTO.getSubCategoryIds().isEmpty()) {
            adminContentMapper.insertExhibitionCurriculumMappings(id, exhibitionDTO.getSubCategoryIds());
        }
        return exhibitionDTO;
    }
    @Transactional
    public AdminPlaceDetailDTO createSciencePlace(AdminPlaceDetailDTO placeDTO, MultipartFile file) {
        placeDTO.setMainImageUrl(handleFileUpload(file, null));
        setCoordinates(placeDTO, placeDTO.getAddressDetail());
        adminContentMapper.insertSciencePlace(placeDTO);
        Long id = placeDTO.getPlaceId();

        if (placeDTO.getGradeIds() != null && !placeDTO.getGradeIds().isEmpty()) {
            adminContentMapper.insertPlaceGradeMappings(id, placeDTO.getGradeIds());
        }
        if (placeDTO.getSubCategoryIds() != null && !placeDTO.getSubCategoryIds().isEmpty()) {
            adminContentMapper.insertPlaceCurriculumMappings(id, placeDTO.getSubCategoryIds());
        }
        return placeDTO;
    }

    // ========== 4. Update (PUT) ==========
    @Transactional
    public AdminHallDetailDTO updateExhibitionHall(Long id, AdminHallDetailDTO hallDTO, MultipartFile file) {
        AdminHallDetailDTO existing = adminContentMapper.findHallDetailDTOById(id);
        hallDTO.setHallId(id);
        hallDTO.setMainImageUrl(handleFileUpload(file, existing.getMainImageUrl()));
        setCoordinates(hallDTO, hallDTO.getAddressDetail());
        adminContentMapper.updateExhibitionHall(hallDTO);
        return hallDTO;
    }
    @Transactional
    public AdminExhibitionDetailDTO updateExhibition(Long id, AdminExhibitionDetailDTO exhibitionDTO, MultipartFile file) {
        AdminExhibitionDetailDTO existing = adminContentMapper.findExhibitionDetailDTOById(id);
        exhibitionDTO.setExhibitionId(id);
        exhibitionDTO.setMainImageUrl(handleFileUpload(file, existing.getMainImageUrl()));
        setCoordinates(exhibitionDTO, exhibitionDTO.getAddressDetail());

        adminContentMapper.updateExhibition(exhibitionDTO);

        adminContentMapper.deleteExhibitionGradeMappings(id);
        adminContentMapper.deleteExhibitionCurriculumMappings(id);

        if (exhibitionDTO.getGradeIds() != null && !exhibitionDTO.getGradeIds().isEmpty()) {
            adminContentMapper.insertExhibitionGradeMappings(id, exhibitionDTO.getGradeIds());
        }
        if (exhibitionDTO.getSubCategoryIds() != null && !exhibitionDTO.getSubCategoryIds().isEmpty()) {
            adminContentMapper.insertExhibitionCurriculumMappings(id, exhibitionDTO.getSubCategoryIds());
        }
        return exhibitionDTO;
    }
    @Transactional
    public AdminPlaceDetailDTO updateSciencePlace(Long id, AdminPlaceDetailDTO placeDTO, MultipartFile file) {
        AdminPlaceDetailDTO existing = adminContentMapper.findPlaceDetailDTOById(id);
        placeDTO.setPlaceId(id);
        placeDTO.setMainImageUrl(handleFileUpload(file, existing.getMainImageUrl()));
        setCoordinates(placeDTO, placeDTO.getAddressDetail());

        adminContentMapper.updateSciencePlace(placeDTO);

        adminContentMapper.deletePlaceGradeMappings(id);
        adminContentMapper.deletePlaceCurriculumMappings(id);

        if (placeDTO.getGradeIds() != null && !placeDTO.getGradeIds().isEmpty()) {
            adminContentMapper.insertPlaceGradeMappings(id, placeDTO.getGradeIds());
        }
        if (placeDTO.getSubCategoryIds() != null && !placeDTO.getSubCategoryIds().isEmpty()) {
            adminContentMapper.insertPlaceCurriculumMappings(id, placeDTO.getSubCategoryIds());
        }
        return placeDTO;
    }

    // ========== 5. Delete (DELETE) ==========
    @Transactional
    public void deleteHall(Long id) {
        adminContentMapper.deleteHall(id);
    }
    @Transactional
    public void deleteExhibition(Long id) {
        // 1. (자식) 교과/학년 매핑 삭제
        adminContentMapper.deleteExhibitionGradeMappings(id);
        adminContentMapper.deleteExhibitionCurriculumMappings(id);

        // 2. [신규] (자식) AI 코스 항목 삭제
        adminContentMapper.deleteAiCourseItemsByExhibitionId(id);

        // 3. [신규] (자식) AI 코스 (시작점) 삭제
        adminContentMapper.deleteAiCoursesByExhibitionId(id);

        // 4. (부모) 전시 삭제
        adminContentMapper.deleteExhibition(id);

        // (참고: review, wishlist, stamp_record는 물리적 FK가 아닌
        // target_type='exhibition' 형태의 논리적 참조이므로,
        // 이 테이블들의 데이터는 '삭제' 대신 '비활성화' 처리를 권장합니다.
        // 일단 FK 제약조건은 이 4개로 해결됩니다.)
    }
    @Transactional
    public void deletePlace(Long id) {
        adminContentMapper.deletePlaceGradeMappings(id);
        adminContentMapper.deletePlaceCurriculumMappings(id);
        adminContentMapper.deletePlace(id);
    }

    // ========== [신규] Modal 공통 데이터 Service ==========

    public List<AdminSimpleHallDTO> getSimpleHallList() {
        return adminContentMapper.findSimpleHallList();
    }

    public List<AdminGradeCategoryDTO> getGradeCategoryList() {
        return adminContentMapper.findAllGradeCategories();
    }

    public List<AdminSubCategoryDetailDTO> getSubCategoryDetailList() {
        return adminContentMapper.findAllSubCategoryDetails();
    }
}