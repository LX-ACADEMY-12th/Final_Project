package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
public class ExhibitionDetailDTO {
    private Long hallId;
	private String exhibitionHallName;
	private BigDecimal latitude;
	private BigDecimal longitude;
	private Date startDate;
	private Date endDate;
	private String admissionFee;
	private String openingHours;
	private String mainImageUrl;
	private String description;
	private BigDecimal averageRating;
	private Long totalReviews;
	private String location; // ex) 국립과천과학관 + 미래전시관

	//  사진 리뷰 총 개수
	private Long totalPhotoReviews;

	// 찜 여부 필드 추가
	private boolean liked;

    // MyBatis에서 받을 문자열 필드
    private String exhibitionListStr;

    // Getter에서 자동 변환
    public String[] getExhibitionList() {
        if (exhibitionListStr != null && !exhibitionListStr.isEmpty()) {
            return exhibitionListStr.split("\\|\\|");
        }
        return new String[0];
    }

    private boolean visited;

    private long userId;

    private List<ExhibitionItem> exhibitionList;
    // 4. 내부 클래스 (DTO 안에 쏙 넣으세요)
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExhibitionItem {
        private String title;
        private String imageUrl;
    }
}
