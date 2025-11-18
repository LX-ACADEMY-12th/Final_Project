package com.example.demo.service;

import com.example.demo.dto.*; // DTO 패키지
import com.example.demo.mapper.AdminContentMapper; // Mapper 패키지
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import com.example.demo.dto.AdminSimpleHallDTO;
import com.example.demo.dto.AdminGradeCategoryDTO;
import com.example.demo.dto.AdminSubCategoryDetailDTO;

@Service
public class AdminContentService {

    private final AdminContentMapper adminContentMapper;
    private final GeocodingService geocodingService;
    private final Storage storage;

    @Value("${gcs.bucket-name}")
    private String bucketName;

    @Autowired
    public AdminContentService(AdminContentMapper adminContentMapper,
                               GeocodingService geocodingService,
                               Storage storage) {
        this.adminContentMapper = adminContentMapper;
        this.geocodingService = geocodingService;
        this.storage = storage;
    }


    // --- 5. [GCS] Utility: GCS 파일 처리 ---

    private String handleFileUpload(MultipartFile file, String existingBlobName) {
        try {
            if (file != null && !file.isEmpty()) {
                if (existingBlobName != null && !existingBlobName.isEmpty()) {
                    BlobId blobId = BlobId.of(bucketName, existingBlobName);
                    storage.delete(blobId);
                }

                String newBlobName = "content/" + UUID.randomUUID().toString() + "-" + file.getOriginalFilename();
                BlobId blobId = BlobId.of(bucketName, newBlobName);
                BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                        .setContentType(file.getContentType())
                        .build();

                storage.create(blobInfo, file.getBytes());

                return newBlobName;
            } else {
                return existingBlobName;
            }
        } catch (IOException e) {
            throw new RuntimeException("GCS 파일 처리 중 오류 발생: " + e.getMessage(), e);
        }
    }

    private String generateSignedUrl(String objectName) {
        if (objectName == null || objectName.isEmpty()) {
            return null;
        }

        try {
            BlobInfo blobInfo = BlobInfo.newBuilder(BlobId.of(bucketName, objectName)).build();
            URL signedUrl = storage.signUrl(blobInfo, 15, TimeUnit.MINUTES, Storage.SignUrlOption.withV4Signature());
            return signedUrl.toString();
        } catch (Exception e) {
            System.err.println("Signed URL 생성 실패 (Object: " + objectName + "): " + e.getMessage());
            return null;
        }
    }

    // --- [GCS] DTO의 BlobName을 Signed URL로 변환하는 헬퍼들 ---

    private AdminHallDetailDTO convertToSignedUrl(AdminHallDetailDTO dto) {
        if (dto != null) {
            dto.setMainImageUrl(generateSignedUrl(dto.getMainImageUrl()));
        }
        return dto;
    }

    private AdminExhibitionDetailDTO convertToSignedUrl(AdminExhibitionDetailDTO dto) {
        if (dto != null) {
            dto.setMainImageUrl(generateSignedUrl(dto.getMainImageUrl()));
        }
        return dto;
    }

    private AdminPlaceDetailDTO convertToSignedUrl(AdminPlaceDetailDTO dto) {
        if (dto != null) {
            dto.setMainImageUrl(generateSignedUrl(dto.getMainImageUrl()));
        }
        return dto;
    }

    // --- Utility: 지오코딩 ---
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
        List<AdminHallListDTO> list = adminContentMapper.findAllHallsForList();
        list.forEach(dto -> dto.setImageUrl(generateSignedUrl(dto.getImageUrl())));
        return list;
    }
    public List<AdminExhibitionListDTO> getExhibitionList(Long hallId) {
        List<AdminExhibitionListDTO> list = adminContentMapper.findAllExhibitionsForList(hallId);
        list.forEach(dto -> dto.setImageUrl(generateSignedUrl(dto.getImageUrl())));
        return list;
    }
    public List<AdminPlaceListDTO> getPlaceList() {
        List<AdminPlaceListDTO> list = adminContentMapper.findAllPlacesForList();
        list.forEach(dto -> dto.setImageUrl(generateSignedUrl(dto.getImageUrl())));
        return list;
    }

    // ========== 2. Detail (GET BY ID) ==========
    public AdminHallDetailDTO getHallById(Long id) {
        AdminHallDetailDTO dto = adminContentMapper.findHallDetailDTOById(id);
        return convertToSignedUrl(dto);
    }
    public AdminExhibitionDetailDTO getExhibitionById(Long id) {
        AdminExhibitionDetailDTO dto = adminContentMapper.findExhibitionDetailDTOById(id);
        if (dto != null) {
            dto.setGradeIds(adminContentMapper.findGradeIdsByExhibitionId(id));
            dto.setSubCategoryIds(adminContentMapper.findSubCategoryIdsByExhibitionId(id));
        }
        return convertToSignedUrl(dto);
    }
    public AdminPlaceDetailDTO getPlaceById(Long id) {
        AdminPlaceDetailDTO dto = adminContentMapper.findPlaceDetailDTOById(id);
        if (dto != null) {
            dto.setGradeIds(adminContentMapper.findGradeIdsByPlaceId(id));
            dto.setSubCategoryIds(adminContentMapper.findSubCategoryIdsByPlaceId(id));
        }
        return convertToSignedUrl(dto);
    }

    // ========== 3. Create (POST) ==========
    @Transactional
    public AdminHallDetailDTO createExhibitionHall(AdminHallDetailDTO hallDTO, MultipartFile file) {
        hallDTO.setMainImageUrl(handleFileUpload(file, null));
        setCoordinates(hallDTO, hallDTO.getAddressDetail());
        adminContentMapper.insertExhibitionHall(hallDTO);

        hallDTO.setMainImageUrl(generateSignedUrl(hallDTO.getMainImageUrl()));
        return hallDTO;
    }
    @Transactional
    public AdminExhibitionDetailDTO createExhibition(AdminExhibitionDetailDTO exhibitionDTO, MultipartFile file) {
        exhibitionDTO.setMainImageUrl(handleFileUpload(file, null));
        exhibitionDTO.setType("PERMANENT");
        setCoordinates(exhibitionDTO, exhibitionDTO.getAddressDetail());
        adminContentMapper.insertExhibition(exhibitionDTO);
        Long id = exhibitionDTO.getExhibitionId();

        if (exhibitionDTO.getGradeIds() != null && !exhibitionDTO.getGradeIds().isEmpty()) {
            adminContentMapper.insertExhibitionGradeMappings(id, exhibitionDTO.getGradeIds());
        }
        if (exhibitionDTO.getSubCategoryIds() != null && !exhibitionDTO.getSubCategoryIds().isEmpty()) {
            adminContentMapper.insertExhibitionCurriculumMappings(id, exhibitionDTO.getSubCategoryIds());
        }

        exhibitionDTO.setMainImageUrl(generateSignedUrl(exhibitionDTO.getMainImageUrl()));
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

        placeDTO.setMainImageUrl(generateSignedUrl(placeDTO.getMainImageUrl()));
        return placeDTO;
    }

    // ========== 4. Update (PUT) ==========
    @Transactional
    public AdminHallDetailDTO updateExhibitionHall(Long id, AdminHallDetailDTO hallDTO, MultipartFile file) {
        AdminHallDetailDTO existing = adminContentMapper.findHallDetailDTOById(id);
        if (existing == null) {
            throw new RuntimeException("Hall not found: " + id);
        }
        hallDTO.setHallId(id);

        AdminHallDetailDTO existingRaw = adminContentMapper.findHallDetailDTOById(id);

        String newBlobName = handleFileUpload(file, existingRaw.getMainImageUrl());
        hallDTO.setMainImageUrl(newBlobName);

        setCoordinates(hallDTO, hallDTO.getAddressDetail());
        adminContentMapper.updateExhibitionHall(hallDTO);

        hallDTO.setMainImageUrl(generateSignedUrl(newBlobName));
        return hallDTO;
    }

    @Transactional
    public AdminExhibitionDetailDTO updateExhibition(Long id, AdminExhibitionDetailDTO exhibitionDTO, MultipartFile file) {
        AdminExhibitionDetailDTO existingRaw = adminContentMapper.findExhibitionDetailDTOById(id);
        if (existingRaw == null) {
            throw new RuntimeException("Exhibition not found: " + id);
        }
        exhibitionDTO.setExhibitionId(id);

        String newBlobName = handleFileUpload(file, existingRaw.getMainImageUrl());
        exhibitionDTO.setMainImageUrl(newBlobName);

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

        exhibitionDTO.setMainImageUrl(generateSignedUrl(newBlobName));
        return exhibitionDTO;
    }

    @Transactional
    public AdminPlaceDetailDTO updateSciencePlace(Long id, AdminPlaceDetailDTO placeDTO, MultipartFile file) {
        AdminPlaceDetailDTO existingRaw = adminContentMapper.findPlaceDetailDTOById(id);
        if (existingRaw == null) {
            throw new RuntimeException("Place not found: " + id);
        }
        placeDTO.setPlaceId(id);

        String newBlobName = handleFileUpload(file, existingRaw.getMainImageUrl());
        placeDTO.setMainImageUrl(newBlobName);

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

        placeDTO.setMainImageUrl(generateSignedUrl(newBlobName));
        return placeDTO;
    }

    // ========== 5. Delete (DELETE) ==========
    @Transactional
    public void deleteHall(Long id) {
        // 1. GCS 파일 삭제
        AdminHallDetailDTO existing = adminContentMapper.findHallDetailDTOById(id);
        if (existing != null && existing.getMainImageUrl() != null && !existing.getMainImageUrl().isEmpty()) {
            storage.delete(BlobId.of(bucketName, existing.getMainImageUrl()));
        }

        // 1-A. 위시리스트 정리
        adminContentMapper.deleteWishlistsByTargetIdAndType(id, "exhibition");

        // 2. [Level 1] 직접 참조 자식 항목 정리
        adminContentMapper.deleteFinalScheduleItemsByHallId(id);
        adminContentMapper.deleteAiCourseItemsByHallId(id);

        // 3. [Level 2] AI 추천 코스(ai_recommended_course) 계층 정리
        List<Long> aiCourseIdsToDelete = adminContentMapper.findAiCourseIdsByHallId(id);

        if (!aiCourseIdsToDelete.isEmpty()) {

            // Final Schedule 정리 (ai_recommended_course 참조 해제)
            List<Long> finalScheduleIdsToDelete = adminContentMapper.findFinalScheduleIdsByAiCourseIds(aiCourseIdsToDelete);
            if (!finalScheduleIdsToDelete.isEmpty()) {
                adminContentMapper.deleteFinalScheduleItemsByScheduleIds(finalScheduleIdsToDelete);
            }
            adminContentMapper.deleteFinalSchedulesByAiCourseIds(aiCourseIdsToDelete);

            // ai_recommend 정리 (ai_recommended_course 참조 해제)
            adminContentMapper.deleteAiRecommendItemsByAiCourseIds(aiCourseIdsToDelete);
            adminContentMapper.deleteAiRecommendsByAiCourseIds(aiCourseIdsToDelete);

            // ai_recommended_course 최종 삭제 (모든 자식 정리 후)
            adminContentMapper.deleteAiCoursesByHallId(id);
        }

        // 4. [Level 2] 전시(exhibition) 계층 정리
        List<Long> exhibitionIdsToDelete = adminContentMapper.findExhibitionIdsByHallId(id);

        if (!exhibitionIdsToDelete.isEmpty()) {
            adminContentMapper.deleteExhibitionGradeMappingsByExhibitionIds(exhibitionIdsToDelete);
            adminContentMapper.deleteExhibitionCurriculumMappingsByExhibitionIds(exhibitionIdsToDelete);
            adminContentMapper.deleteExhibitionsByIds(exhibitionIdsToDelete);
        }

        // 5. [Level 3] Root 테이블 최종 삭제
        adminContentMapper.deleteHall(id);
    }

    @Transactional
    public void deleteExhibition(Long id) {
        // 1. GCS 파일 삭제
        AdminExhibitionDetailDTO existing = adminContentMapper.findExhibitionDetailDTOById(id);
        if (existing != null && existing.getMainImageUrl() != null && !existing.getMainImageUrl().isEmpty()) {
            // storage.delete(BlobId.of(bucketName, existing.getMainImageUrl()));
        }

        // 2. 위시리스트 정리 (전시)
        adminContentMapper.deleteWishlistsByTargetIdAndType(id, "exhibition");

        // 3. N:M 매핑 테이블 정리
        adminContentMapper.deleteExhibitionGradeMappings(id);
        adminContentMapper.deleteExhibitionCurriculumMappings(id);

        // 4. exhibition 테이블 최종 삭제
        adminContentMapper.deleteExhibition(id);
    }

    @Transactional
    public void deletePlace(Long id) {
        // 1. GCS 파일 삭제
        AdminPlaceDetailDTO existing = adminContentMapper.findPlaceDetailDTOById(id);
        if (existing != null && existing.getMainImageUrl() != null && !existing.getMainImageUrl().isEmpty()) {
            storage.delete(BlobId.of(bucketName, existing.getMainImageUrl()));
        }

        // 2. [Level 1] 직접 참조 자식 항목 정리
        adminContentMapper.deleteFinalScheduleItemsByPlaceId(id);
        adminContentMapper.deleteAiCourseItemsByPlaceId(id);
        adminContentMapper.deleteWishlistsByTargetIdAndType(id, "science_place");

        // 3. [Level 2] AI 코스 및 Final Schedule 계층 정리
        List<Long> aiCourseIdsToDelete = adminContentMapper.findAiCourseIdsByPlaceId(id);

        if (!aiCourseIdsToDelete.isEmpty()) {

            // Final Schedule 정리 (ai_recommended_course 참조 해제)
            // 'final_schedule' 테이블의 'source_ai_course_id' 컬럼을 참조하는 경우 findFinalScheduleIdsByAiCourseIds2 사용
            List<Long> finalScheduleIdsToDelete = adminContentMapper.findFinalScheduleIdsByAiCourseIds2(aiCourseIdsToDelete);
            if (!finalScheduleIdsToDelete.isEmpty()) {
                adminContentMapper.deleteFinalScheduleItemsByScheduleIds(finalScheduleIdsToDelete); // FK: final_schedule_item -> final_schedule
            }
            adminContentMapper.deleteFinalSchedulesByAiCourseIds2(aiCourseIdsToDelete); // FK: final_schedule -> ai_recommended_course

            // ai_recommend 정리 (ai_recommended_course 참조 해제)
            adminContentMapper.deleteAiRecommendItemsByAiCourseIds(aiCourseIdsToDelete);
            adminContentMapper.deleteAiRecommendsByAiCourseIds(aiCourseIdsToDelete);

            // ai_recommended_course 최종 삭제 (모든 자식 정리 후)
            adminContentMapper.deleteAiCoursesByPlaceId(id);
        }

        // 4. [Level 3] Root 테이블 최종 삭제
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