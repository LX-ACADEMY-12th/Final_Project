package com.example.demo.mapper;


import com.example.demo.dto.LikedCourseListDTO;
import com.example.demo.dto.ExhibitionDTO; // [!!] 1. '목록용 DTO' import 추가
import com.example.demo.dto.PlaceResultDTO;
import com.example.demo.vo.Exhibition;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface ExhibitionMapper {

    // ID 목록으로 전시 정보 리스트 조회 (목록 카드용)
    List<LikedCourseListDTO> findExhibitionsForLikedList(@Param("ids") List<Long> ids);

    List<ExhibitionDTO> findAllExhibitions(@Param("category") String category);

    // --- [Admin 기능] ---
    Exhibition findById(Long id);
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Exhibition exhibition);
    void update(Exhibition exhibition);
    void deleteById(Long id);
    // [수정] 이 메서드는 이제 사용되지 않지만, 다른 곳에서 쓸 수 있으니 유지합니다.
    List<PlaceResultDTO> findAllForAdmin();
    // --- [페이지네이션용 새 메서드 추가] ---
    /**
     * [새 메서드] 통합된 관리자 목록을 페이징하여 조회
     */
    List<PlaceResultDTO> findCombinedAdminContent(Map<String, Object> params);
    /**
     * [새 메서드] 통합된 관리자 목록의 전체 개수 조회
     */
    int countCombinedAdminContent();
}
