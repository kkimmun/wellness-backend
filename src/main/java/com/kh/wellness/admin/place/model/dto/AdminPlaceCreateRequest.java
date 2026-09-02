package com.kh.wellness.admin.place.model.dto;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

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
public class AdminPlaceCreateRequest {

	@NotNull(message = "분류는 필수입니다.")
	@Positive(message = "올바른 분류를 선택해야 합니다.")
	private Long typeDetailNo;

	@NotBlank(message = "장소명은 필수입니다.")
	@Size(max = 200, message = "장소명은 200자 이하로 입력해야 합니다.")
	private String placeName;

	@Size(max = 2000, message = "장소 설명은 2000자 이하로 입력해야 합니다.")
	private String placeDescription;

	@NotBlank(message = "주소는 필수입니다.")
	@Size(max = 300, message = "주소는 300자 이하로 입력해야 합니다.")
	private String addr;

	@NotNull(message = "경도는 필수입니다.")
	private Double xAxis;

	@NotNull(message = "위도는 필수입니다.")
	private Double yAxis;

	// 이미지는 선택 (0장 등록 허용)
	private List<MultipartFile> imageFiles;
}
