package com.kh.wellness.admin.place.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlaceImageLicenseResponse {

	private String sourceName;
	private String sourcePageUrl;
	private String authorName;
	private String licenseCode;
	private String licenseUrl;
	private String attributionText;
}
