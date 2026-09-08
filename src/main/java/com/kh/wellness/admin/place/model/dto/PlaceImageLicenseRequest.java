package com.kh.wellness.admin.place.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class PlaceImageLicenseRequest {

	@NotNull(message = "이미지 번호를 확인해주세요.")
	@Positive(message = "올바른 이미지 번호를 입력해야 합니다.")
	private Long imgNo;

	@NotBlank(message = "출처명은 필수입니다.")
	@Size(max = 100, message = "출처명은 100자 이하로 입력해야 합니다.")
	private String sourceName;

	@NotBlank(message = "출처 페이지 URL은 필수입니다.")
	@Size(max = 2000, message = "출처 페이지 URL은 2000자 이하로 입력해야 합니다.")
	private String sourcePageUrl;

	@Size(max = 200, message = "저작자명은 200자 이하로 입력해야 합니다.")
	private String authorName;

	@NotBlank(message = "라이선스 코드는 필수입니다.")
	@Size(max = 50, message = "라이선스 코드는 50자 이하로 입력해야 합니다.")
	private String licenseCode;

	@Size(max = 2000, message = "라이선스 URL은 2000자 이하로 입력해야 합니다.")
	private String licenseUrl;

	@NotBlank(message = "귀속 표기 문구는 필수입니다.")
	@Size(max = 1000, message = "귀속 표기 문구는 1000자 이하로 입력해야 합니다.")
	private String attributionText;
}
