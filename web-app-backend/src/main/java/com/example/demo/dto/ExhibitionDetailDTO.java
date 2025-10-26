package com.example.demo.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class ExhibitionDetailDTO {

	private Long exhibitionId;
	private String exhibitionName;
	private Long hallId; // 전시관 아이디
	private BigDecimal latitude;
	private BigDecimal longitude;
	private String type; // 상설 or 기획
	private Date startDate;
	private Date endDate;
	private BigDecimal admissionFee;
	private String openingHours;
	private String mainImageUrl;
	private String description;
	private BigDecimal averageRating;
	private Long totalReviews;
	
}
