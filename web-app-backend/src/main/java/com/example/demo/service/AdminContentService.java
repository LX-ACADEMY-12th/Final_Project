package com.example.demo.service;

import com.example.demo.dto.*; // DTO 패키지
import com.example.demo.mapper.AdminContentMapper; // Mapper 패키지
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import lombok.RequiredArgsConstructor;
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
// @RequiredArgsConstructor // 1. 생성자를 직접 정의하므로 제거
public class AdminContentService {

    private final AdminContentMapper adminContentMapper;
    // 2. 기존 FileUploadService 제거
    // private final FileUploadService fileUploadService;
    private final GeocodingService geocodingService;

    // 3. [GCS] Storage 및 bucketName 주입
    private final Storage storage;

    @Value("${gcs.bucket-name}")
    private String bucketName;

    // 4. [GCS] FileUploadService 대신 Storage를 주입받는 생성자
    @Autowired
    public AdminContentService(AdminContentMapper adminContentMapper,
                               GeocodingService geocodingService,
                               Storage storage) {
        this.adminContentMapper = adminContentMapper;
        this.geocodingService = geocodingService;
        this.storage = storage;
    }


    // --- 5. [GCS] Utility: GCS 파일 처리 (UserService에서 가져옴) ---

    /**
     * GCS에 파일을 업로드/삭제하고 BlobName을 반환합니다.
     * @param file 새로 업로드할 파일 (null이거나 비어있으면 기존 파일 유지/삭제만)
     * @param existingBlobName DB에 저장된 기존 BlobName (GCS 객체 이름)
     * @return DB에 저장할 최종 BlobName (새로 업로드되었거나, 기존 이름)
     */
    private String handleFileUpload(MultipartFile file, String existingBlobName) {
        try {
            // 새 파일이 있는 경우
            if (file != null && !file.isEmpty()) {
                // 기존 파일(BlobName)이 있으면 GCS에서 삭제
                if (existingBlobName != null && !existingBlobName.isEmpty()) {
                    BlobId blobId = BlobId.of(bucketName, existingBlobName);
                    storage.delete(blobId);
                }

                // 새 파일 GCS에 업로드
                // "content/" 폴더 내에 저장 (UserService의 "profiles/"와 구분)
                String newBlobName = "content/" + UUID.randomUUID().toString() + "-" + file.getOriginalFilename();
                BlobId blobId = BlobId.of(bucketName, newBlobName);
                BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                        .setContentType(file.getContentType())
                        .build();

                storage.create(blobInfo, file.getBytes());

                return newBlobName; // DB에 저장될 새 BlobName 반환

                // 새 파일이 없는 경우
            } else {
                return existingBlobName; // 기존 BlobName 유지
            }
        } catch (IOException e) {
            // Checked Exception을 Runtime Exception으로 변환하여 트랜잭션 롤백 유도
            throw new RuntimeException("GCS 파일 처리 중 오류 발생: " + e.getMessage(), e);
        }
    }

    /**
     * GCS 객체 이름(BlobName)을 15분간 유효한 Signed URL로 변환합니다.
     */
    private String generateSignedUrl(String objectName) {
        if (objectName == null || objectName.isEmpty()) {
            return null;
        }

        try {
            BlobInfo blobInfo = BlobInfo.newBuilder(BlobId.of(bucketName, objectName)).build();
            // 15분 제한 시간 설정 (V4 서명 방식)
            URL signedUrl = storage.signUrl(blobInfo, 15, TimeUnit.MINUTES, Storage.SignUrlOption.withV4Signature());
            return signedUrl.toString();
        } catch (Exception e) {
            System.err.println("Signed URL 생성 실패 (Object: " + objectName + "): " + e.getMessage());
            return null; // 실패 시 null 반환
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

    // --- Utility: 지오코딩 (수정 없음) ---
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
    // 6. [GCS] 목록 조회 시에도 Signed URL로 변환
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
    // 7. [GCS] 상세 조회 시 Signed URL 변환 헬퍼 사용
    public AdminHallDetailDTO getHallById(Long id) {
        AdminHallDetailDTO dto = adminContentMapper.findHallDetailDTOById(id);
        return convertToSignedUrl(dto); // BlobName -> Signed URL
    }
    public AdminExhibitionDetailDTO getExhibitionById(Long id) {
        AdminExhibitionDetailDTO dto = adminContentMapper.findExhibitionDetailDTOById(id);
        if (dto != null) {
            dto.setGradeIds(adminContentMapper.findGradeIdsByExhibitionId(id));
            dto.setSubCategoryIds(adminContentMapper.findSubCategoryIdsByExhibitionId(id));
        }
        return convertToSignedUrl(dto); // BlobName -> Signed URL
    }
    public AdminPlaceDetailDTO getPlaceById(Long id) {
        AdminPlaceDetailDTO dto = adminContentMapper.findPlaceDetailDTOById(id);
        if (dto != null) {
            dto.setGradeIds(adminContentMapper.findGradeIdsByPlaceId(id));
            dto.setSubCategoryIds(adminContentMapper.findSubCategoryIdsByPlaceId(id));
        }
        return convertToSignedUrl(dto); // BlobName -> Signed URL
    }

    // ========== 3. Create (POST) ==========
    // 8. [GCS] GCS용 handleFileUpload 헬퍼 호출
    @Transactional
    public AdminHallDetailDTO createExhibitionHall(AdminHallDetailDTO hallDTO, MultipartFile file) {
        // GCS 헬퍼가 호출되어 BlobName을 반환
        hallDTO.setMainImageUrl(handleFileUpload(file, null));
        setCoordinates(hallDTO, hallDTO.getAddressDetail());
        adminContentMapper.insertExhibitionHall(hallDTO);

        // 반환 DTO에도 Signed URL 적용 (필요하다면)
        // return convertToSignedUrl(hallDTO);
        // 혹은 ID로 재조회(getHallById) 하거나,
        // DTO의 BlobName을 Signed URL로 변환하여 반환
        hallDTO.setMainImageUrl(generateSignedUrl(hallDTO.getMainImageUrl()));
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
    // 9. [GCS] GCS용 handleFileUpload 헬퍼 호출
    @Transactional
    public AdminHallDetailDTO updateExhibitionHall(Long id, AdminHallDetailDTO hallDTO, MultipartFile file) {
        AdminHallDetailDTO existing = adminContentMapper.findHallDetailDTOById(id);
        if (existing == null) {
            throw new RuntimeException("Hall not found: " + id); // 또는 적절한 예외 처리
        }
        hallDTO.setHallId(id);
        // GCS 헬퍼가 기존 BlobName(existing.getMainImageUrl())을 받아 처리
        // (주의: existing DTO는 getHallById를 통해 조회했으므로 mainImageUrl이 Signed URL일 수 있습니다.
        //  findHallDetailDTOById가 순수 BlobName을 반환한다고 가정합니다.)
        //  만약 getHallById를 쓴다면, DTO에 blobName 필드를 별도로 두거나,
        //  여기서는 BlobName을 얻기 위한 별도 쿼리(findHallBlobNameById)가 필요할 수 있습니다.
        //  간단하게는 findHallDetailDTOById가 SignedURL 변환 전의 원본 DTO를 반환한다고 가정합니다.

        // **수정된 가정**: getHallById(id)는 Signed URL을 반환하므로,
        // BlobName을 얻기 위해 'findHallDetailDTOById'를 직접 호출합니다.
        AdminHallDetailDTO existingRaw = adminContentMapper.findHallDetailDTOById(id);

        String newBlobName = handleFileUpload(file, existingRaw.getMainImageUrl());
        hallDTO.setMainImageUrl(newBlobName); // DTO에 BlobName 설정

        setCoordinates(hallDTO, hallDTO.getAddressDetail());
        adminContentMapper.updateExhibitionHall(hallDTO);

        hallDTO.setMainImageUrl(generateSignedUrl(newBlobName)); // 반환용 DTO에는 Signed URL 설정
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
        exhibitionDTO.setMainImageUrl(newBlobName); // DB 저장을 위해 BlobName 설정

        setCoordinates(exhibitionDTO, exhibitionDTO.getAddressDetail());
        adminContentMapper.updateExhibition(exhibitionDTO);

        // --- 매핑 테이블 업데이트 ---
        adminContentMapper.deleteExhibitionGradeMappings(id);
        adminContentMapper.deleteExhibitionCurriculumMappings(id);

        if (exhibitionDTO.getGradeIds() != null && !exhibitionDTO.getGradeIds().isEmpty()) {
            adminContentMapper.insertExhibitionGradeMappings(id, exhibitionDTO.getGradeIds());
        }
        if (exhibitionDTO.getSubCategoryIds() != null && !exhibitionDTO.getSubCategoryIds().isEmpty()) {
            adminContentMapper.insertExhibitionCurriculumMappings(id, exhibitionDTO.getSubCategoryIds());
        }

        exhibitionDTO.setMainImageUrl(generateSignedUrl(newBlobName)); // 반환용 DTO에는 Signed URL 설정
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
        placeDTO.setMainImageUrl(newBlobName); // DB 저장을 위해 BlobName 설정

        setCoordinates(placeDTO, placeDTO.getAddressDetail());
        adminContentMapper.updateSciencePlace(placeDTO);

        // --- 매핑 테이블 업데이트 ---
        adminContentMapper.deletePlaceGradeMappings(id);
        adminContentMapper.deletePlaceCurriculumMappings(id);

        if (placeDTO.getGradeIds() != null && !placeDTO.getGradeIds().isEmpty()) {
            adminContentMapper.insertPlaceGradeMappings(id, placeDTO.getGradeIds());
        }
        if (placeDTO.getSubCategoryIds() != null && !placeDTO.getSubCategoryIds().isEmpty()) {
            adminContentMapper.insertPlaceCurriculumMappings(id, placeDTO.getSubCategoryIds());
        }

        placeDTO.setMainImageUrl(generateSignedUrl(newBlobName)); // 반환용 DTO에는 Signed URL 설정
        return placeDTO;
    }

    // ========== 5. Delete (DELETE) ==========
    // 10. [GCS] DB 삭제 전 GCS 파일 삭제 로직 추가
    @Transactional
    public void deleteHall(Long id) {
        // 1. GCS에서 파일 삭제 (BlobName 조회)
        AdminHallDetailDTO existing = adminContentMapper.findHallDetailDTOById(id);
        if (existing != null && existing.getMainImageUrl() != null && !existing.getMainImageUrl().isEmpty()) {
            storage.delete(BlobId.of(bucketName, existing.getMainImageUrl()));
        }
        // 2. DB에서 삭제
        adminContentMapper.deleteHall(id);
    }
    @Transactional
    public void deleteExhibition(Long id) {
        // 1. GCS에서 파일 삭제 (BlobName 조회)
        AdminExhibitionDetailDTO existing = adminContentMapper.findExhibitionDetailDTOById(id);
        if (existing != null && existing.getMainImageUrl() != null && !existing.getMainImageUrl().isEmpty()) {
            storage.delete(BlobId.of(bucketName, existing.getMainImageUrl()));
        }

        // 2. DB에서 자식/부모 레코드 삭제 (기존 로직)
        adminContentMapper.deleteExhibitionGradeMappings(id);
        adminContentMapper.deleteExhibitionCurriculumMappings(id);
        adminContentMapper.deleteAiCourseItemsByExhibitionId(id);
        adminContentMapper.deleteAiCoursesByExhibitionId(id);
        adminContentMapper.deleteExhibition(id);
    }
    @Transactional
    public void deletePlace(Long id) {
        // 1. GCS에서 파일 삭제 (BlobName 조회)
        AdminPlaceDetailDTO existing = adminContentMapper.findPlaceDetailDTOById(id);
        if (existing != null && existing.getMainImageUrl() != null && !existing.getMainImageUrl().isEmpty()) {
            storage.delete(BlobId.of(bucketName, existing.getMainImageUrl()));
        }

        // 2. DB에서 삭제 (기존 로직)
        adminContentMapper.deletePlaceGradeMappings(id);
        adminContentMapper.deletePlaceCurriculumMappings(id);
        adminContentMapper.deletePlace(id);
    }

    // ========== [신규] Modal 공통 데이터 Service ==========
    // (수정 없음)
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