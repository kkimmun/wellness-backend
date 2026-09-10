package com.kh.wellness.member.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import com.kh.wellness.auth.model.vo.CustomUserDetails;
import com.kh.wellness.exception.controller.GlobalExceptionHandler;
import com.kh.wellness.member.model.dto.MemberProfileResponse;
import com.kh.wellness.member.model.service.MemberProfileService;

class MemberProfileControllerTest {
    MemberProfileService service;
    MockMvc mvc;
    @BeforeEach void setup() {
        SecurityContextHolder.clearContext(); service = mock(MemberProfileService.class);
        mvc = MockMvcBuilders.standaloneSetup(new MemberProfileController(service))
            .setControllerAdvice(new GlobalExceptionHandler())
            .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver()).build();
    }
    @org.junit.jupiter.api.AfterEach void clear() { SecurityContextHolder.clearContext(); }
    void login() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(CustomUserDetails.builder().memberNo(7L).build(), null));
    }
    @Test void anonymousRequestIsDenied() throws Exception {
        mvc.perform(get("/api/members/profile")).andExpect(status().isUnauthorized()); verifyNoInteractions(service);
    }
    @Test void profileReturnsCompleteImageUrlWithoutStorageName() throws Exception {
        login();
        var profile = new MemberProfileResponse();
        profile.setImgPath("https://example.com/profile/");
        profile.setSaveName("saved image.png");
        when(service.getProfile(7L)).thenReturn(profile);
        mvc.perform(get("/api/members/profile"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.imgPath").value("https://example.com/profile/saved%20image.png"))
            .andExpect(jsonPath("$.data.saveName").doesNotExist());
    }
    @Test void targetMemberComesFromPrincipalNotBody() throws Exception {
        login(); when(service.updateName(7L, "새닉네임")).thenReturn(new MemberProfileResponse());
        mvc.perform(patch("/api/members/profile").contentType("application/json").content("{\"memberName\":\"새닉네임\",\"memberNo\":999}"))
            .andExpect(status().isOk()); verify(service).updateName(7L, "새닉네임");
    }
    @Test void nicknameValidationRejectsBlankAndLong() throws Exception {
        login();
        for (String name : new String[]{" ", "a", "1234567890123"}) {
            mvc.perform(patch("/api/members/profile").contentType("application/json").content("{\"memberName\":\"" + name + "\"}"))
                .andExpect(status().isBadRequest());
        }
        verifyNoInteractions(service);
    }
    @Test void passwordValidationRunsBeforeService() throws Exception {
        login();
        mvc.perform(patch("/api/members/profile/password").contentType("application/json").content("{\"currentPassword\":\"old123\",\"newPassword\":\"abc\"}"))
            .andExpect(status().isBadRequest()); verifyNoInteractions(service);
    }
}
