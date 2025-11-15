package com.example.demo.dto;

import java.util.Date;

import lombok.Data;

// 프론트에 응답을 보내주는..
@Data
public class StampResponseDTO {

	private Long recordId;
	private Long userId;
	private String targetType;
	private Long targetId;
	private String authMethod;
	private Date visitedAt;

    private String targetName;
}
