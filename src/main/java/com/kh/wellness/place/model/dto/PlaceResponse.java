package com.kh.wellness.place.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PlaceResponse {
	private Long placeNo;
	private String placeName;
	private Double xAxis;
	private Double yAxis;
}
