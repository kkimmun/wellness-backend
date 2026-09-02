package com.kh.wellness.admin.place.model.dto;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlaceNosRequest {

	@NotEmpty(message = "대상 장소를 선택해야 합니다.")
	private List<Long> placeNos;
}
