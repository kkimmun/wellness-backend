package com.kh.wellness.course.model.dto;

import java.util.List;

public class CourseResponse {
	private Long courseNo;
	private List<PlaceDto>places;
	private Long startPlace;
	private Long endPlace;
	private String courseName;
	private int estimatedTime;
	private String description;
	private String active;
}
