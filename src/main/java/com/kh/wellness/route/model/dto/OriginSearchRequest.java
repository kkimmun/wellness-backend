package com.kh.wellness.route.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OriginSearchRequest {

    @NotBlank(message = "검색어는 필수입니다.")
    private String query;
}
