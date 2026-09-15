package com.kh.wellness.plan.model.dto;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PlanResponseDto {
	private Long planNo;
	private String planName;
	private Double xAxis;
	private Double yAxis;
	private Date createDate;
}
