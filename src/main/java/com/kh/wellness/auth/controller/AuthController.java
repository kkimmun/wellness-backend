package com.kh.wellness.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kh.wellness.auth.model.dto.LoginRequestDto;
import com.kh.wellness.auth.model.dto.LoginResponse;
import com.kh.wellness.auth.model.service.AuthService;
import com.kh.wellness.auth.model.vo.CustomUserDetails;
import com.kh.wellness.common.api.ApiResponse;
import com.kh.wellness.token.model.service.TokenService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController 
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
	private final AuthService authService;
	private final TokenService tokenService;

	@PostMapping("/login")
	public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequestDto lrd){
		LoginResponse res = authService.login(lrd);
		
		return ResponseEntity.ok(ApiResponse.success("로그인 성공", res));
	}
	@PostMapping("/logout")
	public ResponseEntity<ApiResponse<Void>> logout(@AuthenticationPrincipal CustomUserDetails userDetails){
		Long memberNoFromToken = userDetails.getMemberNo();
		tokenService.logout(memberNoFromToken);
		
		return ResponseEntity.status(200).body(ApiResponse.success("로그아웃 성공", null));
	}
		
}