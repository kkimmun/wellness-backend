package com.kh.wellness.admin.course.model.vo;

import lombok.Builder;
import lombok.Value;


@Builder
@Value
public class Course {

    private Long courseNo;
    private Long startPlace;
    private Long endPlace;
    private String courseName;
    private String description;
    private String active;
}
