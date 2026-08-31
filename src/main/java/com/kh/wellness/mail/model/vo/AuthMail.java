package com.kh.wellness.mail.model.vo;

import java.sql.Date;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class AuthMail {
	private String  emailAddr;
	private String authCode;
	private Date expDate;
	private String authYn;
	private String userIp;

}
