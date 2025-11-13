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