package com.example.demo.mapper;

import com.example.demo.dto.PlaceResultDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface PlaceMapper {

    // --- 전시 검색 ---
    @Select("SELECT exhibition_id as id, exhibition_name as title, main_image_url as imageUrl, " +
            "       latitude as lat, longitude as lng, description, address_detail as place, " + // 필요한 컬럼 추가
            "       (SELECT category_name FROM curriculum_main_category WHERE main_category_id = csc.main_category_id) as subject, " +
            "       (SELECT grade_name FROM grade_category WHERE grade_id = csc.grade_id) as grade " + // 학년 이름 가져오기
            "FROM exhibition e " +
            "JOIN exhibition_curriculum_mapping ecm ON e.exhibition_id = ecm.exhibition_id " +
            "JOIN curriculum_sub_category csc ON ecm.sub_category_id = csc.sub_category_id " +
            "JOIN curriculum_main_category cmc ON csc.main_category_id = cmc.main_category_id " +
            "WHERE cmc.category_name = #{subject} AND csc.grade_id = (SELECT grade_id FROM grade_category WHERE grade_name LIKE '%' || #{grade})") // 학년 이름으로 ID 찾기
    List<PlaceResultDTO> findExhibitionsByFilter(@Param("subject") String subject, @Param("grade") String grade);


    // PostGIS 사용 (geometry 타입 컬럼 'geom' 가정)
    @Select("SELECT exhibition_id as id, exhibition_name as title, main_image_url as imageUrl, " +
            "       latitude as lat, longitude as lng, description, address_detail as place, " +
            "       (SELECT category_name FROM curriculum_main_category WHERE main_category_id = csc.main_category_id) as subject, " +
            "       (SELECT grade_name FROM grade_category WHERE grade_id = csc.grade_id) as grade " +
            "FROM exhibition e " +
            "JOIN exhibition_curriculum_mapping ecm ON e.exhibition_id = ecm.exhibition_id " +
            "JOIN curriculum_sub_category csc ON ecm.sub_category_id = csc.sub_category_id " +
            "JOIN curriculum_main_category cmc ON csc.main_category_id = cmc.main_category_id " +
            "WHERE cmc.category_name = #{subject} AND csc.grade_id = (SELECT grade_id FROM grade_category WHERE grade_name LIKE '%' || #{grade}) " +
            // ST_DWithin(geography 컬럼, 기준점, 반경(미터))
            "AND ST_DWithin(ST_MakePoint(longitude, latitude)::geography, ST_MakePoint(#{lng}, #{lat})::geography, #{radiusMeters})")
    List<PlaceResultDTO> findExhibitionsByRadius(@Param("subject") String subject, @Param("grade") String grade,
                                                 @Param("lat") double lat, @Param("lng") double lng, @Param("radiusMeters") double radiusMeters);

    @Select("SELECT exhibition_id as id, exhibition_name as title, main_image_url as imageUrl, " +
            "       latitude as lat, longitude as lng, description, address_detail as place, " +
            "       (SELECT category_name FROM curriculum_main_category WHERE main_category_id = csc.main_category_id) as subject, " +
            "       (SELECT grade_name FROM grade_category WHERE grade_id = csc.grade_id) as grade " +
            "FROM exhibition e " +
            "JOIN exhibition_curriculum_mapping ecm ON e.exhibition_id = ecm.exhibition_id " +
            "JOIN curriculum_sub_category csc ON ecm.sub_category_id = csc.sub_category_id " +
            "JOIN curriculum_main_category cmc ON csc.main_category_id = cmc.main_category_id " +
            "WHERE cmc.category_name = #{subject} AND csc.grade_id = (SELECT grade_id FROM grade_category WHERE grade_name LIKE '%' || #{grade}) " +
            "AND address_detail LIKE #{regionPattern}") // 주소 컬럼에 LIKE 사용
    List<PlaceResultDTO> findExhibitionsByRegion(@Param("subject") String subject, @Param("grade") String grade, @Param("regionPattern") String regionPattern);


    // --- 과학 장소(답사) 검색 (유사하게 작성) ---
    @Select("SELECT place_id as id, place_name as title, main_image_url as imageUrl, " +
            "       latitude as lat, longitude as lng, description, address_detail as place, " +
            "       (SELECT category_name FROM curriculum_main_category WHERE main_category_id = csc.main_category_id) as subject, " +
            "       (SELECT grade_name FROM grade_category WHERE grade_id = csc.grade_id) as grade " +
            "FROM science_place sp " +
            "JOIN place_curriculum_mapping pcm ON sp.place_id = pcm.place_id " +
            "JOIN curriculum_sub_category csc ON pcm.sub_category_id = csc.sub_category_id " +
            "JOIN curriculum_main_category cmc ON csc.main_category_id = cmc.main_category_id " +
            "WHERE cmc.category_name = #{subject} AND csc.grade_id = (SELECT grade_id FROM grade_category WHERE grade_name LIKE '%' || #{grade})")
    List<PlaceResultDTO> findPlacesByFilter(@Param("subject") String subject, @Param("grade") String grade);

    @Select("SELECT place_id as id, place_name as title, main_image_url as imageUrl, " +
            "       latitude as lat, longitude as lng, description, address_detail as place, " +
            "       (SELECT category_name FROM curriculum_main_category WHERE main_category_id = csc.main_category_id) as subject, " +
            "       (SELECT grade_name FROM grade_category WHERE grade_id = csc.grade_id) as grade " +
            "FROM science_place sp " +
            "JOIN place_curriculum_mapping pcm ON sp.place_id = pcm.place_id " +
            "JOIN curriculum_sub_category csc ON pcm.sub_category_id = csc.sub_category_id " +
            "JOIN curriculum_main_category cmc ON csc.main_category_id = cmc.main_category_id " +
            "WHERE cmc.category_name = #{subject} AND csc.grade_id = (SELECT grade_id FROM grade_category WHERE grade_name LIKE '%' || #{grade}) " +
            "AND ST_DWithin(ST_MakePoint(longitude, latitude)::geography, ST_MakePoint(#{lng}, #{lat})::geography, #{radiusMeters})")
    List<PlaceResultDTO> findPlacesByRadius(@Param("subject") String subject, @Param("grade") String grade,
                                            @Param("lat") double lat, @Param("lng") double lng, @Param("radiusMeters") double radiusMeters);

    @Select("SELECT place_id as id, place_name as title, main_image_url as imageUrl, " +
            "       latitude as lat, longitude as lng, description, address_detail as place, " +
            "       (SELECT category_name FROM curriculum_main_category WHERE main_category_id = csc.main_category_id) as subject, " +
            "       (SELECT grade_name FROM grade_category WHERE grade_id = csc.grade_id) as grade " +
            "FROM science_place sp " +
            "JOIN place_curriculum_mapping pcm ON sp.place_id = pcm.place_id " +
            "JOIN curriculum_sub_category csc ON pcm.sub_category_id = csc.sub_category_id " +
            "JOIN curriculum_main_category cmc ON csc.main_category_id = cmc.main_category_id " +
            "WHERE cmc.category_name = #{subject} AND csc.grade_id = (SELECT grade_id FROM grade_category WHERE grade_name LIKE '%' || #{grade}) " +
            "AND address_detail LIKE #{regionPattern}")
    List<PlaceResultDTO> findPlacesByRegion(@Param("subject") String subject, @Param("grade") String grade, @Param("regionPattern") String regionPattern);

}