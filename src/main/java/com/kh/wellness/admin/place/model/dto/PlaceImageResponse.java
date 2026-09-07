package com.kh.wellness.admin.place.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlaceImageResponse {

	private Long imgNo;
	private Integer imgOrder;
	private String imgPath;
	private String originalName;
	private String saveName;
	private String imageUrl;
	private PlaceImageLicenseResponse license;
}
