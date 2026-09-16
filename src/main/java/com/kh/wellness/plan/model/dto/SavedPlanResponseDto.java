package com.kh.wellness.plan.model.dto;

import java.util.Date;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SavedPlanResponseDto {
	private Long planNo;
	private String planName;
	private Double xAxis;
	private Double yAxis;
	private Date createDate;
	private List<PlanPlaceResponseDto> places;
}
