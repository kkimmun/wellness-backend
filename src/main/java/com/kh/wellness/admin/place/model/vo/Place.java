package com.kh.wellness.admin.place.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Place {

	private Long placeNo;
	private Long typeDetailNo;
	private String placeName;
	private String placeDescription;
	private String addr;
	private Double xAxis;
	private Double yAxis;
	private Integer viewCount;
}
