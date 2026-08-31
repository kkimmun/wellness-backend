package com.kh.wellness.admin.cource.model.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AdminCourseRequest {

    @NotBlank(message = "코스명은 필수입니다.")
    @Size(max = 100, message = "코스명은 100자 이하로 입력해야 합니다.")
    private String courseName;

    @NotNull(message = "예상 소요시간은 필수입니다.")
    @Positive(message = "예상 소요시간은 1분 이상이어야 합니다.")
    private Integer estimatedTime;

    @NotBlank(message = "코스 설명은 필수입니다.")
    @Size(max = 500, message = "코스 설명은 500자 이하로 입력해야 합니다.")
    private String description;

    @NotNull(message = "출발지는 필수입니다.")
    @Positive(message = "올바른 출발지를 선택해야 합니다.")
    private Long startPlaceNo;

    @Size(max = 3, message = "중간 관광지는 최대 3개까지 선택할 수 있습니다.")
    private List<@NotNull(message = "중간 관광지를 확인해주세요.")
            @Positive(message = "올바른 중간 관광지를 선택해야 합니다.") Long> waypointPlaceNos;

    @NotNull(message = "도착지는 필수입니다.")
    @Positive(message = "올바른 도착지를 선택해야 합니다.")
    private Long endPlaceNo;
}
