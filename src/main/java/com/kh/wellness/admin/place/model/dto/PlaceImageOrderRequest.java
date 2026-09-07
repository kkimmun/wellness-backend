package com.kh.wellness.admin.place.model.dto;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PlaceImageOrderRequest {

	@NotEmpty(message = "이미지 순서를 입력해야 합니다.")
	@Size(max = 50, message = "이미지는 최대 50개까지 정렬할 수 있습니다.")
	private List<@NotNull(message = "이미지 번호를 확인해주세요.")
			@Positive(message = "올바른 이미지 번호를 입력해야 합니다.") Long> imgNos;
}
