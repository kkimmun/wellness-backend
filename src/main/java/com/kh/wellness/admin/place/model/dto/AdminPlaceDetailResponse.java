package com.kh.wellness.admin.place.model.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdminPlaceDetailResponse {

	private String createDate;
	private String placeName;
	private String placeDescription;
	private Double xAxis;
	private Double yAxis;
	private List<PlaceImageResponse> placeImages;
}
