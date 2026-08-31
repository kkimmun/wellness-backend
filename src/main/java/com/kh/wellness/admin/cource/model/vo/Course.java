package com.kh.wellness.admin.cource.model.vo;

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
public class Course {

    private Long courseNo;
    private Long startPlace;
    private Long endPlace;
    private String courseName;
    private Integer estimatedTime;
    private String description;
    private String active;
}
