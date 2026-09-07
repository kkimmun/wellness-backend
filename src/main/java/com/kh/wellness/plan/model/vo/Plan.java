package com.kh.wellness.plan.model.vo;

import lombok.Builder;
import lombok.Getter;
import lombok.Value;

@Getter
@Value
@Builder
public class Plan {
	private Long memberNo;
	private Long placeNo;
	private Integer placeOrder;
	
}
