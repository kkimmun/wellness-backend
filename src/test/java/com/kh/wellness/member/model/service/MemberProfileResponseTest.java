package com.kh.wellness.member.model.service;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import com.kh.wellness.member.model.dto.MemberProfileResponse;
import tools.jackson.databind.json.JsonMapper;

class MemberProfileResponseTest {
    @Test void directoryAndNameBecomeCompleteUrlWithoutExposingSaveName() throws Exception {
        var profile = new MemberProfileResponse();
        profile.setImgPath("https://example.com/profile/"); profile.setSaveName("new photo.png");
        var json = JsonMapper.builder().build().valueToTree(profile);
        assertThat(json.get("imgPath").toString()).isEqualTo("\"https://example.com/profile/new%20photo.png\"");
        assertThat(json.has("saveName")).isFalse();
        assertThat(profile.getSaveName()).isEqualTo("new photo.png");
    }
    @Test void completeUrlIsNotAppendedAgain() {
        var profile = new MemberProfileResponse();
        profile.setImgPath("https://example.com/profile/photo.png"); profile.setSaveName("photo.png");
        assertThat(profile.getImgPath()).isEqualTo("https://example.com/profile/photo.png");
    }
    @Test void missingPhotoDoesNotReturnDirectoryAsImage() {
        var profile = new MemberProfileResponse();
        assertThat(profile.getImgPath()).isNull();
        profile.setImgPath("https://example.com/profile/");
        assertThat(profile.getImgPath()).isNull();
    }
}
