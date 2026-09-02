package com.kh.wellness.route.model.dto;

import java.util.List;

import com.kh.wellness.course.model.dto.PlaceDto;
import com.kh.wellness.course.model.enums.CourseTag;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@AllArgsConstructor
@ToString
public class PlaceCandidate {

    private PlaceDto place;
    private double distance;
    private double distanceScore;
    private double tagScore;
    private double totalScore;
    private String placeName;
    private String imageUrl;
    private List<CourseTag> tags;
}