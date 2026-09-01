package com.kh.wellness.token.model.vo;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class RefreshToken {
	private Long memberNo;
	private String token;
	private Long expirationDate;
}
