package com.kh.wellness.admin.course.model.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CourseNosRequest {

    @Valid
    @NotEmpty(message = "삭제할 코스를 선택해야 합니다.")
    private List<@NotNull(message = "코스 번호는 필수입니다.")
            @Positive(message = "올바르지 않은 코스 번호입니다.") Long> courseNos;
}
