package com.example.demo.mapper;

import com.example.demo.dto.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.example.demo.dto.AdminSimpleHallDTO;
import com.example.demo.dto.AdminGradeCategoryDTO;
import com.example.demo.dto.AdminSubCategoryDetailDTO;
import java.util.List;

@Mapper
public interface AdminContentMapper {

    // 1. List
    List<AdminHallListDTO> findAllHallsForList();
    List<AdminExhibitionListDTO> findAllExhibitionsForList(@Param("hallId") Long hallId);
    List<AdminPlaceListDTO> findAllPlacesForList();

    // 2. Detail
    AdminHallDetailDTO findHallDetailDTOById(Long id);
    AdminExhibitionDetailDTO findExhibitionDetailDTOById(Long id);
    AdminPlaceDetailDTO findPlaceDetailDTOById(Long id);

    // 3. Detail (N:M ID Lists)
    List<Long> findGradeIdsByExhibitionId(Long id);
    List<Long> findSubCategoryIdsByExhibitionId(Long id);
    List<Long> findGradeIdsByPlaceId(Long id);
    List<Long> findSubCategoryIdsByPlaceId(Long id);

    // 4. Create
    int insertExhibitionHall(AdminHallDetailDTO hallDTO);
    int insertExhibition(AdminExhibitionDetailDTO exhibitionDTO);
    int insertSciencePlace(AdminPlaceDetailDTO placeDTO);

    // 5. Update
    int updateExhibitionHall(AdminHallDetailDTO hallDTO);
    int updateExhibition(AdminExhibitionDetailDTO exhibitionDTO);
    int updateSciencePlace(AdminPlaceDetailDTO placeDTO);

    // 6. Delete
    int deleteHall(Long id);
    int deleteAiCourseItemsByExhibitionId(Long exhibitionId);
    int deleteAiCoursesByExhibitionId(Long exhibitionId);
    int deleteExhibition(Long id);
    int deletePlace(Long id);

    // ====================================================================
    // 6-A. [추가] 외래 키 종속성 해결을 위한 Mapper 메서드
    // ====================================================================

    // 6-A-1. AI 코스 ID 목록 조회 (삭제할 AI 코스 목록을 먼저 식별)
    List<Long> findAiCourseIdsByExhibitionId(Long exhibitionId);

    // 6-A-2. final_schedule에서 AI 코스 ID 목록을 참조하는 행 삭제 (FK 오류 해결)
    int deleteFinalSchedulesByAiCourseIds(@Param("list") List<Long> aiCourseIds);

    // 6-A-3. ai_course_item에서 AI 코스 ID 목록을 참조하는 행 삭제 (이전 FK 오류 해결)
    int deleteAiCourseItemsByAiCourseIds(@Param("list") List<Long> aiCourseIds);

    // 7. N:M Mappings
    int insertExhibitionGradeMappings(@Param("exhibitionId") Long exhibitionId, @Param("gradeIds") List<Long> gradeIds);
    int insertExhibitionCurriculumMappings(@Param("exhibitionId") Long exhibitionId, @Param("subCategoryIds") List<Long> subCategoryIds);
    int insertPlaceGradeMappings(@Param("placeId") Long placeId, @Param("gradeIds") List<Long> gradeIds);
    int insertPlaceCurriculumMappings(@Param("placeId") Long placeId, @Param("subCategoryIds") List<Long> subCategoryIds);

    int deleteExhibitionGradeMappings(Long id);
    int deleteExhibitionCurriculumMappings(Long id);
    int deletePlaceGradeMappings(Long id);
    int deletePlaceCurriculumMappings(Long id);

    // ========== [신규] Modal 공통 데이터 Mappers ==========

    List<AdminSimpleHallDTO> findSimpleHallList();
    List<AdminGradeCategoryDTO> findAllGradeCategories();
    List<AdminSubCategoryDetailDTO> findAllSubCategoryDetails();
}