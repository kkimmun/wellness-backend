package com.kh.wellness.admin.cource.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CourseStatusRequest {

    @NotBlank(message = "코스 상태는 필수입니다.")
    @Pattern(regexp = "Y|N", message = "올바르지 않은 코스 상태입니다.")
    private String active;
}
