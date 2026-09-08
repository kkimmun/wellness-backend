package com.kh.wellness.admin.place.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaceLicense {

	private Long imgNo;
	private String sourceName;
	private String sourcePageUrl;
	private String authorName;
	private String licenseCode;
	private String licenseUrl;
	private String attributionText;
}
