package com.kh.wellness.token.model.service;

import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kh.wellness.auth.model.vo.CustomUserDetails;
import com.kh.wellness.exception.UnauthorizedException;
import com.kh.wellness.token.model.dao.TokenMapper;
import com.kh.wellness.token.model.vo.RefreshToken;
import com.kh.wellness.token.util.JwtUtil;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TokenService {
	private final JwtUtil tokenUtil;
	private final TokenMapper tokenMapper;
	
	public Map<String, String> getTokens(CustomUserDetails user) {
		Map<String, String> tokens = createTokens(user);
				
		saveToken(tokens.get("refreshToken"), user);
		
		return tokens;
	}
	
	// 토큰 생성 및 반환 메소드
	@Transactional
	private Map<String,String> createTokens(CustomUserDetails user) {
		return Map.of("accessToken", tokenUtil.getAccessToken(user),
						"refreshToken", tokenUtil.getRefreshToken(user));
	}
	
	// 리프레시토큰을 받아서 DB에 INSERT 메소드
	private void saveToken(String token, CustomUserDetails user) { 
		log.info("저장할 memberNo 확인: {}", user.getMemberNo());
		RefreshToken refreshToken = RefreshToken.builder()
										.memberNo(user.getMemberNo())
										.token(token)
										.expirationToken(System.currentTimeMillis()+ (1000L * 60 * 60 * 24 * 5)) // 5일로 고정
										.build();
		tokenMapper.saveToken(refreshToken);
	}
	// 로그아웃 요청 시 DB정리 메서드
	@Transactional
	public void logout(Long memberNo) {
		tokenMapper.deleteToken(memberNo);
	}
	// 추후 AccessToken이 만료기간이 지나서 토큰 갱신 요청이 들어왔을때
	// 사용자에게 전달받은 RefreshToken이 DB에 존재하면서 만료기간이 지나지 않았는지 검증
	
	public Map<String, String> tokenLocation(String refreshToken){
		RefreshToken token = tokenMapper.findByToken(refreshToken);
		if(token == null || token.getExpirationToken() < System.currentTimeMillis()) {
			throw new UnauthorizedException("유효하지 않은 토큰입니다.");
		}
		Claims claims = tokenUtil.parseJwt(token.getToken());

		Long memberNo = Long.valueOf(claims.getSubject());

		
		CustomUserDetails user = CustomUserDetails.builder().memberNo(memberNo).build();
		return createTokens(user);
	}
	
	
	
	
}
