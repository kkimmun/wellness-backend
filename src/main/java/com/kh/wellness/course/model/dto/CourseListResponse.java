package com.kh.wellness.course.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CourseListResponse {

    private Long courseNo;
    private String courseName;
    private PlaceResponse startPlace;
    private PlaceResponse endPlace;
    private Integer estimatedTime;
    private String description;

    public static CourseListResponse from(CourseListRow row) {
        return new CourseListResponse(
                row.getCourseNo(),
                row.getCourseName(),
                new PlaceResponse(row.getStartPlaceNo(), row.getStartPlaceName()),
                new PlaceResponse(row.getEndPlaceNo(), row.getEndPlaceName()),
                row.getEstimatedTime(),
                row.getDescription());
    }
}
