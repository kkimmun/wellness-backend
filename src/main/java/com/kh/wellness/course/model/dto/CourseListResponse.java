package com.kh.wellness.course.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CourseListResponse {

    private Long courseNo;
    private String courseName;
    private CoursePlaceResponse startPlace;
    private CoursePlaceResponse endPlace;
    private String description;

    public static CourseListResponse from(CourseListRow row) {
        return new CourseListResponse(
                row.getCourseNo(),
                row.getCourseName(),
                new CoursePlaceResponse(row.getStartPlaceNo(), row.getStartPlaceName()),
                new CoursePlaceResponse(row.getEndPlaceNo(), row.getEndPlaceName()),
                row.getDescription());
    }
}
