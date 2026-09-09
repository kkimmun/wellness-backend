package com.kh.wellness.plan.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class PlanPlaceResponseDto {
	private Long placeNo;
	private Integer placeOrder;
	private String placeName;
	private String placeDescription;
	private String addr;
	private String addrDetail;
	private Double xAxis;
	private Double yAxis;
	private Long typeNo;
	private Long typeDetailNo;
	private String type;
	private String typeDetail;
	private String imageUrl;

}
