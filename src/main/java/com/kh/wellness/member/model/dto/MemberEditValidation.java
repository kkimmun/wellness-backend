package com.kh.wellness.member.model.dto;

import java.sql.Date;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class MemberEditValidation {
	@NotBlank
    private String memberPwd;
    @Pattern(
        regexp = "^.{2,12}$",
        message = "이름은 2~12자여야 합니다."
    )
    @NotBlank
    private String memberName;
    @NotBlank
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;
    private Date editDate;
}