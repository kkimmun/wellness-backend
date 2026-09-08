package com.kh.wellness.recommendation.course.model.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CourseRecommendationCandidateRow {

    private Long placeNo;
    private String placeName;
    private String placeDescription;
    private String addr;
    private String addrDetail;
    private Double xAxis;
    private Double yAxis;
    private Long typeNo;
    private String type;
    private Long typeDetailNo;
    private String typeDetail;
    private Long viewCount;
    private String imageUrl;
    private Long tagNo;
    private String tagContent;
}
