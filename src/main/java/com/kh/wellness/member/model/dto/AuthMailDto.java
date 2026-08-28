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
public class AuthMailDto {
	private String emailAddr;
	private Integer authCode;
	private Integer expDate;
	private String userIp;
}
