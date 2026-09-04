package com.kh.wellness.admin.course.model.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
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

	@NotBlank(message = "코스 설명은 필수입니다.")
	@Size(max = 500, message = "코스 설명은 500자 이하로 입력해야 합니다.")
	private String description;

	@NotNull(message = "출발지는 필수입니다.")
	@Positive(message = "올바른 출발지를 선택해야 합니다.")
	private Long startPlaceNo;

	@Size(max = 3, message = "중간 관광지는 최대 3개까지 선택할 수 있습니다.")
	private List<@NotNull(message = "중간 관광지를 확인해주세요.") @Positive(message = "올바른 중간 관광지를 선택해야 합니다.") Long> waypointPlaceNos;

	@NotNull(message = "도착지는 필수입니다.")
	@Positive(message = "올바른 도착지를 선택해야 합니다.")
	private Long endPlaceNo;

    @Valid
    @Size(max = 3, message = "중간 관광지는 최대 3개까지 선택할 수 있습니다.")
    private List<@NotNull(message = "중간 관광지를 확인해주세요.") AdminCourseWaypointRequest> waypoints;

    // 기존 번호 목록 요청도 지원한다. 설명을 입력할 때는 waypoints를 사용한다.
    public List<Long> getWaypointPlaceNos() {
        return waypoints == null ? waypointPlaceNos : waypoints.stream()
                .map(waypoint -> waypoint == null ? null : waypoint.getPlaceNo())
                .toList();
    }

    @JsonIgnore
    @AssertTrue(message = "중간 관광지 번호 목록과 상세 목록이 일치해야 합니다.")
    public boolean isWaypointSelectionConsistent() {
        return waypoints == null || waypointPlaceNos == null
                || waypointPlaceNos.equals(getWaypointPlaceNos());
    }
}
