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

    // 6. Delete (기존 매퍼)
    int deleteHall(Long id);
    int deleteExhibition(Long id);
    int deletePlace(Long id);

    // 6. Delete (기존에 있었으나 이제 사용하지 않을 가능성이 높은 단일 삭제 매퍼들)
    int deleteAiCourseItemsByExhibitionId(Long exhibitionId);
    int deleteAiCoursesByExhibitionHallId(Long exhibitionId);
    int deleteExhibitionGradeMappings(Long id);
    int deleteExhibitionCurriculumMappings(@Param("id") Long id);
    int deletePlaceGradeMappings(@Param("id") Long id);
    int deletePlaceCurriculumMappings(@Param("id") Long id);


    // ====================================================================
    // 6-A. [추가] 외래 키 종속성 해결을 위한 Mapper 메서드 (Hall 로직용)
    // ====================================================================

    // A. AI 코스 계층 정리 (Hall ID 기준)
    @Deprecated
    List<Long> findAiCourseIdsByHallId(@Param("hallId") Long hallId);

    int deleteFinalScheduleItemsByHallId(@Param("hallId") Long hallId);
    int deleteAiCourseItemsByHallId(@Param("hallId") Long hallId);

    // final_schedule ID 조회 (Hall용 - source_inner_course_id)
    List<Long> findFinalScheduleIdsByAiCourseIds(@Param("list") List<Long> aiCourseIds);
    // final_schedule ID 조회 (Place용 - source_ai_course_id)
    List<Long> findFinalScheduleIdsByAiCourseIds2(@Param("list") List<Long> aiCourseIds);

    // final_schedule 삭제 (Hall용)
    int deleteFinalSchedulesByAiCourseIds(@Param("list") List<Long> aiCourseIds);
    // final_schedule 삭제 (Place용)
    int deleteFinalSchedulesByAiCourseIds2(@Param("list") List<Long> aiCourseIds);

    // final_schedule_item 정리 (schedule_id 기준)
    int deleteFinalScheduleItemsByScheduleIds(@Param("list") List<Long> aiCourseIds);

    int deleteAiCoursesByHallId(@Param("hallId") Long hallId);


    // B. Exhibition 계층 정리 (Hall ID 기준)
    List<Long> findExhibitionIdsByHallId(@Param("hallId") Long hallId);
    int deleteExhibitionGradeMappingsByExhibitionIds(@Param("list") List<Long> exhibitionIds);
    int deleteExhibitionCurriculumMappingsByExhibitionIds(@Param("list") List<Long> exhibitionIds);
    int deleteExhibitionsByIds(@Param("list") List<Long> exhibitionIds);

    // C. Wishlist 정리
    int deleteWishlistsByTargetIdAndType(@Param("targetId") Long targetId, @Param("targetType") String targetType);


    // D. [신규] Place 삭제 전용 매퍼
    int deleteFinalScheduleItemsByPlaceId(@Param("placeId") Long id);
    int deleteAiCourseItemsByPlaceId(@Param("placeId") Long id);
    List<Long> findAiCourseIdsByPlaceId(@Param("placeId") Long id);
    int deleteAiCoursesByPlaceId(@Param("placeId") Long id);

    // ai_recommend 정리 (ai_recommended_course_id 기준)
    int deleteAiRecommendItemsByAiCourseIds(@Param("list") List<Long> aiCourseIds);
    int deleteAiRecommendsByAiCourseIds(@Param("list") List<Long> aiCourseIds);

    // 7. N:M Mappings (Insert)
    int insertExhibitionGradeMappings(@Param("exhibitionId") Long exhibitionId, @Param("gradeIds") List<Long> gradeIds);
    int insertExhibitionCurriculumMappings(@Param("exhibitionId") Long exhibitionId, @Param("subCategoryIds") List<Long> subCategoryIds);
    int insertPlaceGradeMappings(@Param("placeId") Long placeId, @Param("gradeIds") List<Long> gradeIds);
    int insertPlaceCurriculumMappings(@Param("placeId") Long placeId, @Param("subCategoryIds") List<Long> subCategoryIds);

    // 8. Modal 공통 데이터 Mappers
    List<AdminSimpleHallDTO> findSimpleHallList();
    List<AdminGradeCategoryDTO> findAllGradeCategories();
    List<AdminSubCategoryDetailDTO> findAllSubCategoryDetails();

    @Deprecated
    List<Long> findAiCourseIdsByExhibitionHallId(Long exhibitionIHallId);
    @Deprecated
    int deleteAiCourseItemsByAiCourseIds(@Param("list") List<Long> aiCourseIds);
}