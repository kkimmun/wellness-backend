package com.kh.wellness.admin.place.model.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PlaceImageLicenseBatchRequest {

	@NotNull(message = "라이선스 목록을 확인해주세요.")
	@Size(max = 50, message = "라이선스는 최대 50개까지 저장할 수 있습니다.")
	private List<@Valid PlaceImageLicenseRequest> licenses;
}
