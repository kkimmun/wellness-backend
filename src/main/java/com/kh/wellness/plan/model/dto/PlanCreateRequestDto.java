package com.kh.wellness.plan.model.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

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
	@NotBlank(message = "계획 이름을 입력해주세요.")
	@Size(max = 30, message = "계획 이름은 30자 이내로 입력해주세요.")
	private String planName;

	@NotNull(message = "시작 위치의 경도를 입력해주세요.")
	@DecimalMin(value = "-180.0", message = "경도는 -180 이상이어야 합니다.")
	@DecimalMax(value = "180.0", message = "경도는 180 이하여야 합니다.")
	private Double xAxis;

	@NotNull(message = "시작 위치의 위도를 입력해주세요.")
	@DecimalMin(value = "-90.0", message = "위도는 -90 이상이어야 합니다.")
	@DecimalMax(value = "90.0", message = "위도는 90 이하여야 합니다.")
	private Double yAxis;

	@Valid
	@NotEmpty(message = "계획에는 한 개 이상의 장소가 필요합니다.")
	@Size(max = 10, message = "계획에는 장소를 최대 10개까지 저장할 수 있습니다.")
	private List<PlanPlaceRequestDto> places;
}
