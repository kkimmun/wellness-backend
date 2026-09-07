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
public class PlanPlaceRequestDto {
	private Long placeNo;
	private Integer placeOrder;

}
