package com.kh.wellness.auth.model.service;

import java.util.Map;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kh.wellness.auth.model.dto.LoginRequestDto;
import com.kh.wellness.auth.model.dto.LoginResponse;
import com.kh.wellness.auth.model.dto.LoginResult;
import com.kh.wellness.auth.model.dto.TokenResponse;
import com.kh.wellness.auth.model.vo.CustomUserDetails;
import com.kh.wellness.exception.NotFoundException;
import com.kh.wellness.exception.UnauthorizedException;
import com.kh.wellness.token.model.service.TokenService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class AuthService {
	private final AuthenticationManager authenticationManager;
	private final TokenService tokenService;

	public LoginResult login(LoginRequestDto lrd) {
		Authentication auth = null;
		
		try {
			auth = authenticationManager
					.authenticate(new UsernamePasswordAuthenticationToken(lrd.getMemberId(), lrd.getMemberPwd()));
		} catch (AuthenticationException e) {
			throw new NotFoundException("아이디 또는 비밀번호가 이상합니다");
		}

		//인증 성공함
		CustomUserDetails user = (CustomUserDetails) auth.getPrincipal();
		// 토큰 발급
			
			Map<String, String> tokens = tokenService.getTokens(user);
			
		    LoginResponse userInfo = LoginResponse.builder()
		            .memberNo(user.getMemberNo())
		            .memberId(user.getUsername())
		            .role(user.getAuthorities().iterator().next().getAuthority())
		            .build();

		    return LoginResult.builder()
		            .userInfo(userInfo)
		            .accessToken(tokens.get("accessToken"))
		            .refreshToken(tokens.get("refreshToken"))
		            .build();
	}

	public TokenResponse refresh(String refreshToken) {
		if (refreshToken == null || refreshToken.isBlank()) {
			throw new UnauthorizedException("리프레시 토큰이 없습니다.");
		}

		Map<String, String> tokens = tokenService.tokenLocation(refreshToken);

		return new TokenResponse(tokens.get("accessToken"), tokens.get("refreshToken"));
	}


}
