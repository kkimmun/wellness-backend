package com.kh.wellness.admin.cource.model.vo;

import lombok.Builder;
import lombok.Value;


@Builder
@Value
public class Course {

    private Long courseNo;
    private Long startPlace;
    private Long endPlace;
    private String courseName;
    private Integer estimatedTime;
    private String description;
    private String active;
}
