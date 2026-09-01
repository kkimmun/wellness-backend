package com.kh.wellness.course.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CourseListRow {

    private Long courseNo;
    private String courseName;
    private Long startPlaceNo;
    private String startPlaceName;
    private Long endPlaceNo;
    private String endPlaceName;
    private Integer estimatedTime;
    private String description;
}
