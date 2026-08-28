package com.kh.wellness.member.model.vo;

import lombok.Builder;
import lombok.Getter;
import lombok.Value;

@Getter
@Value
@Builder
public class NormalMember {
	private Long memberNo;
	private String memberId;
	private String memberPwd;
}