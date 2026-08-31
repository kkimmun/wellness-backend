package com.kh.wellness.member.model.vo;

import java.sql.Date;

import lombok.Builder;
import lombok.Getter;
import lombok.Value;

@Getter
@Value
@Builder
public class Member {
	private Long memberNo;
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