package com.kh.wellness.course.model.dto;

import java.util.List;

import com.kh.wellness.course.model.enums.CourseTag;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class PlaceDto {
	private Long placeNo;
	private String placeName;
	private String placeDescription;
	private String addr;
	private String type;
	private Double xAxis;
	private Double yAxis;
	private String imageUrl;
	private List<CourseTag>tags;
}
