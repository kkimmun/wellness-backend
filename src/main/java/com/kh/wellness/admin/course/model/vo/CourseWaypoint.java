package com.kh.wellness.admin.course.model.vo;

import lombok.Builder;
import lombok.Value;


@Builder
@Value
public class CourseWaypoint {
    private Long waypointNo;
    private Long courseNo;
    private Long placeNo;
    private Integer waypointSequence;
	private String placeName;
	private String type;
	private Double xAxis;
	private Double yAxis;
	private String imageUrl;
}
