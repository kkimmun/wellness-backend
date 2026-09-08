package com.kh.wellness.admin.place.model.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdminPlaceUpdateRequest {

	// 전부 선택값 - 보낸 필드만 수정된다 (PATCH)

	@Positive(message = "올바른 분류를 선택해야 합니다.")
	private Long typeDetailNo;

	@Size(max = 200, message = "장소명은 200자 이하로 입력해야 합니다.")
	private String placeName;

	@Size(max = 2000, message = "장소 설명은 2000자 이하로 입력해야 합니다.")
	private String placeDescription;

	@Size(max = 300, message = "주소는 300자 이하로 입력해야 합니다.")
	private String addr;

	private Double xAxis;

	private Double yAxis;

	// imageFiles 와 같은 인덱스의 신규 이미지 라이선스 정보
	private List<@Valid PlaceImageLicenseInput> imageLicenses;

	public boolean hasFieldToUpdate() {
		return typeDetailNo != null
				|| placeName != null
				|| placeDescription != null
				|| addr != null
				|| xAxis != null
				|| yAxis != null;
	}
}
