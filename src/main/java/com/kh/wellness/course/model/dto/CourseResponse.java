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
	private List<PlaceDto>places;
	private Long startPlace;
	private Long endPlace;
	private String courseName;
	private int estimatedTime;
	private String description;
}
