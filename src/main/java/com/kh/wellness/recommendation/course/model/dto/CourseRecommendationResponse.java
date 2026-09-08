package com.kh.wellness.recommendation.course.model.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseRecommendationResponse {

    private String courseSignature;
    private Integer placeCount;
    private Long totalDistanceMeters;
    private Integer matchedTagCount;
    private List<CourseRecommendationPlaceResponse> places;
}
