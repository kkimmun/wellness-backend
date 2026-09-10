package com.kh.wellness.member.model.dto;

import java.time.LocalDateTime;
import java.nio.charset.StandardCharsets;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.web.util.UriUtils;
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
    @JsonIgnore
    private String saveName;
    private LocalDateTime enrollDate;

    // DB 매핑용 저장명은 내부에 유지하고, API에는 바로 사용할 수 있는 URL만 제공한다.
    public String getImgPath() {
        if (imgPath == null || imgPath.isBlank()) return null;
        if (!imgPath.endsWith("/")) return imgPath;
        if (saveName == null || saveName.isBlank()) return null;
        return imgPath + UriUtils.encodePathSegment(saveName, StandardCharsets.UTF_8);
    }
}
