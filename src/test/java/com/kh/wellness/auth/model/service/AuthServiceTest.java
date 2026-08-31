package com.kh.wellness.auth.model.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.kh.wellness.auth.model.dto.LoginRequestDto;
import com.kh.wellness.auth.model.dto.LoginResult;
import com.kh.wellness.auth.model.vo.CustomUserDetails;
import com.kh.wellness.exception.NotFoundException;
import com.kh.wellness.token.model.service.TokenService;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

	@Mock
	private AuthenticationManager authenticationManager;

	@Mock
	private TokenService tokenService;

	@InjectMocks
	private AuthService authService;

	private CustomUserDetails userDetails;

	@BeforeEach
	void setUp() {
		userDetails = CustomUserDetails.builder()
				.memberNo(1L)
				.username("test@wellness.com")
				.password("encodedPwd")
				.memberName("tester")
				.authorities(List.of(new SimpleGrantedAuthority("ROLE_USER")))
				.status("Y")
				.build();
	}

	@Test
	@DisplayName("로그인 성공 시 인증된 사용자 정보와 발급된 토큰을 담아 반환한다")
	void login_success() {
		LoginRequestDto request = new LoginRequestDto("test@wellness.com", "rawPwd");

		Authentication authentication = org.mockito.Mockito.mock(Authentication.class);
		when(authentication.getPrincipal()).thenReturn(userDetails);
		when(authenticationManager.authenticate(any())).thenReturn(authentication);
		when(tokenService.getTokens(userDetails))
				.thenReturn(Map.of("accessToken", "AT", "refreshToken", "RT"));

		LoginResult result = authService.login(request);

		assertThat(result.getAccessToken()).isEqualTo("AT");
		assertThat(result.getRefreshToken()).isEqualTo("RT");
		assertThat(result.getUserInfo().getMemberNo()).isEqualTo(1L);
		assertThat(result.getUserInfo().getMemberId()).isEqualTo("test@wellness.com");
		assertThat(result.getUserInfo().getRole()).isEqualTo("ROLE_USER");
	}

	@Test
	@DisplayName("인증에 실패하면 NotFoundException 을 던지고 토큰을 발급하지 않는다")
	void login_authenticationFailure() {
		LoginRequestDto request = new LoginRequestDto("test@wellness.com", "wrongPwd");
		when(authenticationManager.authenticate(any()))
				.thenThrow(new BadCredentialsException("bad credentials"));

		assertThatThrownBy(() -> authService.login(request))
				.isInstanceOf(NotFoundException.class)
				.hasMessage("아이디 또는 비밀번호가 이상합니다");

		verify(tokenService, never()).getTokens(any());
	}
}
