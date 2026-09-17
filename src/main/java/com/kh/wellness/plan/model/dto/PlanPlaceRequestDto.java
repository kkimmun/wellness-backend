package com.kh.wellness.plan.model.dto;

import jakarta.validation.constraints.NotNull;

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
	@NotNull(message = "장소 번호를 입력해주세요.")
	private Long placeNo;
	private Integer placeOrder;

}
