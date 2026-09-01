package com.kh.wellness.auth.controller;

import java.time.Duration;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kh.wellness.auth.model.dto.LoginRequestDto;
import com.kh.wellness.auth.model.dto.LoginResponse;
import com.kh.wellness.auth.model.dto.LoginResult;
import com.kh.wellness.auth.model.dto.TokenResponse;
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
	public ResponseEntity<ApiResponse<LoginResponse>> login(
	        @Valid @RequestBody LoginRequestDto lrd) {

	    LoginResult res = authService.login(lrd);

	    ResponseCookie refreshCookie = ResponseCookie.from("refreshToken",res.getRefreshToken())
			    .httpOnly(true)
			    .secure(true)
			    .path("/api/auth/refresh")
			    .maxAge(Duration.ofMinutes(60))
			    .sameSite("Lax")
			    .build();
	    
	    LoginResponse loginResponse = LoginResponse.builder()
			    .accessToken(res.getAccessToken())
			    .build();

	    return ResponseEntity.ok()
	            .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
	            .body(ApiResponse.success("로그인 성공", loginResponse));
	}
	
	@PostMapping("/logout")
	public ResponseEntity<ApiResponse<Void>> logout(@AuthenticationPrincipal CustomUserDetails userDetails){
		Long memberNoFromToken = userDetails.getMemberNo();
		
		tokenService.logout(memberNoFromToken);
		
        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", "")
	            .httpOnly(true)
	            .secure(true)
	            .path("/api/auth/refresh")
	            .maxAge(0)
	            .sameSite("Lax")
	            .build();
		
		return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(ApiResponse.success("로그아웃 성공", null));
	}
	
	@PostMapping("/refresh")
	public ResponseEntity<Void> refresh(
	        @CookieValue(value = "refreshToken", required = false) String refreshToken) {

	    TokenResponse res = authService.refresh(refreshToken);

	    ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", res.getRefreshToken())
			    .httpOnly(true)
			    .secure(true)
			    .path("/api/auth/refresh")
			    .maxAge(Duration.ofMinutes(60))
			    .sameSite("Lax")
			    .build();

	    return ResponseEntity.ok()
	            .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
	            .build();
	}
		
}