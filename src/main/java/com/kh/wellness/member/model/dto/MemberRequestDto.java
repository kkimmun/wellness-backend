package com.kh.wellness.member.model.dto;

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
	private String memberName;
	private String email;
	private String imgPath;
    private String saveName;
    private String delYn;

}
