package com.example.demo.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MappingMapper {
    // 1. 이름으로 ID 조회
    Long getGradeIdByName(@Param("gradeName") String gradeName);
    
    // (UI에서 Sub Category를 받지 않으므로) 메인 과목에 속하는 임의의 Sub Category ID 조회
    Long getSubCategoryIdBySubjectName(@Param("subjectName") String subjectName);
    
    // 2. 매핑 정보 삭제 (UPDATE 시 기존 정보 초기화)
    void deleteExhibitionGradeMapping(@Param("contentId") Long contentId);
    void deleteExhibitionCurriculumMapping(@Param("contentId") Long contentId);
    void deletePlaceGradeMapping(@Param("contentId") Long contentId);
    void deletePlaceCurriculumMapping(@Param("contentId") Long contentId);

    // 3. 매핑 정보 저장
    void insertExhibitionGradeMapping(@Param("contentId") Long contentId, @Param("gradeId") Long gradeId);
    void insertExhibitionCurriculumMapping(@Param("contentId") Long contentId, @Param("subCategoryId") Long subCategoryId);
    void insertPlaceGradeMapping(@Param("contentId") Long contentId, @Param("gradeId") Long gradeId);
    void insertPlaceCurriculumMapping(@Param("contentId") Long contentId, @Param("subCategoryId") Long subCategoryId);
}