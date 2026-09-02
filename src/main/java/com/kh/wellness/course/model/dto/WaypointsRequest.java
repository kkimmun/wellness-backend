package com.kh.wellness.course.model.dto;

import java.util.List;

import com.kh.wellness.course.model.enums.CourseTag;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WaypointsRequest {
	@Positive(message = "올바른 도착 장소를 선택해야 합니다.")
	private Long endPlaceNo;
    @DecimalMin(value = "-180.0", message = "출발지 경도는 -180 이상이어야 합니다.")
    @DecimalMax(value = "180.0", message = "출발지 경도는 180 이하여야 합니다.")
    private Double startX;
    @DecimalMin(value = "-90.0", message = "출발지 위도는 -90 이상이어야 합니다.")
    @DecimalMax(value = "90.0", message = "출발지 위도는 90 이하여야 합니다.")
    private Double startY;
    @Size(max=5, message="태그는 최대 5개까지 선택할 수 있습니다.")
    private List<CourseTag>tags;
    @NotNull(message = "소요시간을 선택해야 합니다.")
    @Positive(message = "소요시간은 0보다 커야 합니다.")
    private int estimatedTime;
}
