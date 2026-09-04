package com.kh.wellness.admin.course.model.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AdminCourseWaypointRequest {
    @NotNull(message = "중간 관광지는 필수입니다.")
    @Positive(message = "올바른 중간 관광지를 선택해야 합니다.")
    private Long placeNo;

    // 해당 코스에서 이 관광지가 갖는 순례길 서사
    private String waypointDescription;
}