package com.example.demo.dto;

import lombok.Data;
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
	private boolean isLiked;

    // MyBatis에서 받을 문자열 필드
    private String exhibitionListStr;
    // 실제 사용할 배열
    private String[] exhibitionList;
    // Getter에서 자동 변환
    public String[] getExhibitionList() {
        if (exhibitionListStr != null && !exhibitionListStr.isEmpty()) {
            return exhibitionListStr.split("\\|\\|");
        }
        return new String[0];
    }
}
