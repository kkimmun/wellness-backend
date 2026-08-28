package com.kh.wellness.member.model.dto;

import java.sql.Date;

import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MemberDto {
    private Long memberNo;
    @Email( message = "아이디는 이메일 형식이어야 합니다." )
    @NotBlank(message = "아이디는 필수값입니다.")
    private String memberId;
    @NotBlank(message = "비밀번호는 필수값입니다.")
    @Pattern(
        regexp = "^(?=.*[a-zA-Z])(?=.*\\d)[a-zA-Z\\d]{6,15}$",
        message = "비밀번호는 공백 없이 영문과 숫자를 포함한 6~15자여야 합니다."
    )
    private String memberPwd;
    @NotBlank(message = "닉네임은 필수 값입니다.")
    @Pattern(
        regexp = "^\\S{2,12}$",
        message = "닉네임은 공백 없이 2~12자여야 합니다."
    )
    private String memberName;
    private String role;
    private String phoneNumber;
    private String originalName;
    private String saveName;
    private String imgPath;
    private Date enrollDate;
    private String socialProvider;
    private String socialId;
    private String delYn;
    private Date delDate;
}
