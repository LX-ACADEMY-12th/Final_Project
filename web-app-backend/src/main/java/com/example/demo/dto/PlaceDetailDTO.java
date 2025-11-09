package com.example.demo.dto; // (패키지는 맞게 수정하세요)

import lombok.Data;
import java.math.BigDecimal;
// java.util.Date 대신 java.time.LocalDate/LocalDateTime을 권장하지만, 
// ExhibitionDTO와 통일성을 위해 일단 둡니다.
// (science_place 테이블엔 날짜가 없어서 DTO엔 불필요)

@Data
public class PlaceDetailDTO {

    // 1. 'science_place' 테이블의 기본 정보
    private Long placeId;
    private String placeName;
    private String description;
    private String mainImageUrl;
    private String addressDetail; // (ExhibitionDTO의 'location'과 비슷한 역할)
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String admissionFee;
    private String openingHours;
    private String placeType;
    
    // 2. 'review' 요약 정보 (science_place 테이블에 이미 있음)
    private BigDecimal averageRating;
    private Long totalReviews;

    // 3. Join으로 나오는 값들 (ExhibitionDTO와 동일한 방식)
    /**
     * 'place_curriculum_mapping' -> ... -> 'curriculum_main_category'
     * (e.g., "과학, 사회")
     */
    private String categoryName; 
    
    /**
     * 'place_curriculum_mapping' -> 'curriculum_sub_category'
     * (e.g., "자석의 이용, 우리 고장")
     */
    private String subCategoryName;
    
    /**
     * 'place_grade_mapping' -> 'grade_category'
     * (e.g., "3학년, 4학년")
     */
    private String grade;
    
    // 사진 리뷰 총 개수
 	private Long totalPhotoReviews;
    
 	// 찜 여부 필드 추가
 	private boolean liked;

    private boolean visited;

    private long userId;

}