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
	    regexp = "^[가-힣a-zA-Z0-9]{2,12}$",
	    message = "닉네임은 한글, 영문, 숫자만 2~12자로 입력해주세요."
	)
    @NotBlank
    private String memberName;
    @NotBlank
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;
    private Date editDate;
}