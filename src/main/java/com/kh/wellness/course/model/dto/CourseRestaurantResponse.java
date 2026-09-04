package com.kh.wellness.course.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CourseRestaurantResponse {
    private PlaceDto place;
    // 음식점에서 선택한 경로까지의 최단 거리 (미터). 실제 도보 이동 거리가 아니다.
    private double distance;
}
