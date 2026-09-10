package com.kh.wellness.member.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

// 비밀번호는 로그(toString)나 응답 직렬화에 포함하지 않는다.
@Getter
@Setter
public class PasswordUpdateRequest {
    @NotBlank(message = "현재 비밀번호를 입력해주세요.")
    @Size(max = 72)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String currentPassword;

    @NotBlank(message = "새 비밀번호를 입력해주세요.")
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d)[a-zA-Z\\d]{6,15}$",
             message = "비밀번호는 영문과 숫자를 포함한 6~15자여야 합니다.")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String newPassword;
}
