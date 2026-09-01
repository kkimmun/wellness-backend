package com.kh.wellness.course.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class WaypointDto {
    private Long waypointNo;
    private Long courseNo;
    private Long placeNo;
    private Integer waypointSequence;
	private String placeName;
	private Double xAxis;
	private Double yAxis;
	private String imageUrl;
}
