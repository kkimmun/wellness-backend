package com.kh.wellness.plan.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class PlanCreateRequestDto {
	private String planName;
	private Double xAxis;
	private Double yAxis;
}
