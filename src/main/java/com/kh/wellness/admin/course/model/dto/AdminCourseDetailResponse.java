package com.kh.wellness.admin.course.model.dto;

import java.util.List;
import com.kh.wellness.course.model.dto.WaypointDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AdminCourseDetailResponse {
    private Long courseNo;
    private String courseName;
    private String description;
    private Long startPlaceNo;
    private Long endPlaceNo;
    private String active;
    private List<Long> waypointPlaceNos;
    private List<WaypointDto> waypoints;
}
