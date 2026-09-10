package com.kh.wellness.member.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProfileUpdateRequest {
    @NotBlank(message = "닉네임을 입력해주세요.")
    @Pattern(regexp = "^\\S{2,12}$", message = "닉네임은 공백 없이 2~12자여야 합니다.")
    private String memberName;
}
