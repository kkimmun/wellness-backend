package com.kh.wellness.route.model.dto;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
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
public class RouteSearchRequest {

    @Positive(message = "올바른 도착 장소를 선택해야 합니다.")
    private Long endPlaceNo;

    @Positive(message = "올바른 출발 장소를 선택해야 합니다.")
    private Long startPlaceNo;

    @DecimalMin(value = "-180.0", message = "출발지 경도는 -180 이상이어야 합니다.")
    @DecimalMax(value = "180.0", message = "출발지 경도는 180 이하여야 합니다.")
    private Double startX;

    @DecimalMin(value = "-90.0", message = "출발지 위도는 -90 이상이어야 합니다.")
    @DecimalMax(value = "90.0", message = "출발지 위도는 90 이하여야 합니다.")
    private Double startY;

    @DecimalMin(value = "-180.0", message = "도착지 경도는 -180 이상이어야 합니다.")
    @DecimalMax(value = "180.0", message = "도착지 경도는 180 이하여야 합니다.")
    private Double endX;

    @DecimalMin(value = "-90.0", message = "도착지 위도는 -90 이상이어야 합니다.")
    @DecimalMax(value = "90.0", message = "도착지 위도는 90 이하여야 합니다.")
    private Double endY;

    @NotBlank(message = "이동수단은 필수입니다.")
    private String transportType;

    private String routeOption;
    private String transitType;
    private String sortType;

    @Size(max = 3, message = "경유지는 최대 3개까지 설정할 수 있습니다.")
    private List<
            @NotNull(message = "경유 장소 번호는 필수입니다.")
            @Positive(message = "올바른 경유 장소를 선택해야 합니다.")
            Long> waypointPlaceNos = new ArrayList<>();
}
