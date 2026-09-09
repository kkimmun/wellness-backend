package com.kh.wellness.recommendation.course.model.dto;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CourseRecommendationRequest {

    @NotNull
    @DecimalMin("-180.0")
    @DecimalMax("180.0")
    private Double startX;

    @NotNull
    @DecimalMin("-90.0")
    @DecimalMax("90.0")
    private Double startY;

    @Min(3)
    @Max(10)
    private Integer placeCount = 5;

    @Size(max = 2)
    private List<Long> preferredPlaceNos = new ArrayList<>();

    @Size(max = 2)
    private List<Long> tagNos = new ArrayList<>();

    @Size(max = 30)
    private List<String> excludeCourseSignatures = new ArrayList<>();
}
