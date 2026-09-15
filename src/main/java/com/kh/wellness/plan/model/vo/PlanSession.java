package com.kh.wellness.plan.model.vo;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PlanSession {
	private Long planNo;
	private Long placeNo;
	private Integer placeOrder;
}
