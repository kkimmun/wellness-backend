package com.kh.wellness.member.model.dto;

import java.time.LocalDateTime;

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
public class MemberRequestDto {
	private Long memberNo;
	private String memberId;
	private String memberName;
	private String role;
	private String originalName;
	private String saveName;
	private String imgPath;
	private String phoneNumber;
	private LocalDateTime enrollDate;
	private String delYn;

}
