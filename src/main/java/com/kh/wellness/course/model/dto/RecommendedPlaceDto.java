package com.kh.wellness.course.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class RecommendedPlaceDto {
    private PlaceDto place;

    private double distance;
    private double distanceScore;
    private double tagScore;
    private double totalScore;
}

