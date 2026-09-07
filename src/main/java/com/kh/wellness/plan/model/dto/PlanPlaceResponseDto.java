package com.kh.wellness.plan.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class PlanPlaceResponseDto {
	private Long placeNo;
	private Integer placeOrder;
	private String placeName;

}
