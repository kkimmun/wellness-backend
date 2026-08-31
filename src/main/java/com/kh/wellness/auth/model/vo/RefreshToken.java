package com.kh.wellness.auth.model.vo;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RefreshToken {
    private Long memberNo;
    private String token;
    private LocalDateTime expirationDate;
}
