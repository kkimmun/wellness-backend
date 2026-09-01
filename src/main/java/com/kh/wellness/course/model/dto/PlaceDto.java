package com.kh.wellness.course.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PlaceDto {
	private Long placeNo;
	private String placeName;
	private String type;
	private Double xAxis;
	private Double yAxis;
	private String imageUrl;
}
