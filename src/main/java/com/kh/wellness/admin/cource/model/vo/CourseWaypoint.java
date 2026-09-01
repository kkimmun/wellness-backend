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
public class CourseWaypoint {

    private Long waypointNo;
    private Long courseNo;
    private Long placeNo;
    private Integer waypointSequence;
}
