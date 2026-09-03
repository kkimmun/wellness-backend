package com.kh.wellness.course.model.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
public class CourseResponse {
	private Long courseNo;
	private List<WaypointDto>waypoints;
	private List<PlaceDto>places;
	private Long startPlace;
	private Long endPlace;
	private String endPlaceImg;
	private String courseName;
	private String description;
}
