package com.kh.wellness.member.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.kh.wellness.auth.model.vo.CustomUserDetails;
import com.kh.wellness.common.api.ApiResponse;
import com.kh.wellness.exception.UnauthorizedException;
import com.kh.wellness.member.model.dto.MemberProfileResponse;
import com.kh.wellness.member.model.dto.PasswordUpdateRequest;
import com.kh.wellness.member.model.dto.ProfileUpdateRequest;
import com.kh.wellness.member.model.service.MemberProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/members/profile")
@RequiredArgsConstructor
public class MemberProfileController {
    private final MemberProfileService service;

    @GetMapping
    public ApiResponse<MemberProfileResponse> get(@AuthenticationPrincipal CustomUserDetails user) {
        return ApiResponse.success("회원정보 조회 성공", service.getProfile(memberNo(user)));
    }

    @PatchMapping
    public ApiResponse<MemberProfileResponse> update(@AuthenticationPrincipal CustomUserDetails user,
                                                     @Valid @RequestBody ProfileUpdateRequest request) {
        return ApiResponse.success("닉네임을 변경했습니다.", service.updateName(memberNo(user), request.getMemberName()));
    }

    @PatchMapping("/password")
    public ApiResponse<Void> password(@AuthenticationPrincipal CustomUserDetails user,
                                     @Valid @RequestBody PasswordUpdateRequest request) {
        service.updatePassword(memberNo(user), request);
        return ApiResponse.success("비밀번호를 변경했습니다. 다시 로그인해주세요.", null);
    }

    @PostMapping(value = "/photo", consumes = "multipart/form-data")
    public ApiResponse<MemberProfileResponse> photo(@AuthenticationPrincipal CustomUserDetails user,
                                                    @RequestParam("imageFile") MultipartFile file) {
        return ApiResponse.success("프로필 사진을 변경했습니다.", service.updatePhoto(memberNo(user), file));
    }

    @DeleteMapping("/photo")
    public ApiResponse<MemberProfileResponse> removePhoto(@AuthenticationPrincipal CustomUserDetails user) {
        return ApiResponse.success("기본 프로필로 변경했습니다.", service.removePhoto(memberNo(user)));
    }

    private Long memberNo(CustomUserDetails user) {
        if (user == null || user.getMemberNo() == null) throw new UnauthorizedException("로그인이 필요합니다.");
        return user.getMemberNo();
    }
}
