package com.kh.wellness.admin.place.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdminPlaceListResponse {

	private Long placeNo;
	private String createDate;
	private String placeName;
	private String type;
	private String phoneNumber;
	private String delYn;
}
