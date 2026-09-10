package com.kh.wellness.member.model.dto;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberProfileResponse {
    private Long memberNo;
    private String memberId;
    private String memberName;
    private String role;
    private String socialProvider;
    private String imgPath;
    private String saveName;
    private LocalDateTime enrollDate;
}
