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
public class PlanDetailResponse {
    private Long placeNo;
    private Long typeDetailNo;
    private Long typeNo;
    private String placeName;
    private String placeDescription;
    private String addr;
    private String addrDetail;
    private Long distance;
    private Double xAxis;
    private Double yAxis;
    private String imgPath;
}
