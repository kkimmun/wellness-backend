package com.kh.wellness.plan.model.vo;

import java.util.Date;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Plan {
	private Long planNo;
	private Long memberNo;
	private String planName;
	private Double xAxis;
	private Double yAxis;
	private Date createDate;
}
