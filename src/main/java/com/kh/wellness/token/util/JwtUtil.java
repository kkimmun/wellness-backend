package com.kh.wellness.token.util;

import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.kh.wellness.auth.model.vo.CustomUserDetails;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JwtUtil {

	@Value("${jwt.secret}")
	private String secretKey;
	private SecretKey key;
	
	@PostConstruct
	public void init() {
		byte[] arr =Base64.getDecoder().decode(secretKey);
		this.key = Keys.hmacShaKeyFor(arr);
		
	}

	public String getAccessToken(CustomUserDetails user) {
		return Jwts.builder()
				.subject(String.valueOf(user.getMemberNo()))
				.issuedAt(new Date())
		       .expiration(Date.from(Instant.now().plus(Duration.ofMinutes(15))))
		       .claim("role", resolveRole(user))
		       .signWith(key)
		       .compact();
	}

	public String getRefreshToken(CustomUserDetails user) {
		return  Jwts.builder()
				.subject(String.valueOf(user.getMemberNo()))
				.issuedAt(new Date())
				.expiration(Date.from(Instant.now().plus(Duration.ofMinutes(60))))
				.claim("role", resolveRole(user))
				.signWith(key)
				.compact();
	}

	// 권한 컬렉션에서 순수 권한명(ROLE_ 접두어 제거)을 뽑아 페이로드 claim 으로 사용한다.
	private String resolveRole(CustomUserDetails user) {
		if (user.getAuthorities() == null || user.getAuthorities().isEmpty()) {
			return null;
		}
		String authority = user.getAuthorities().iterator().next().getAuthority();
		if (authority == null) {
			return null;
		}
		return authority.startsWith("ROLE_") ? authority.substring(5) : authority;
	}
	
	
	public Claims parseJwt(String token) {
		return Jwts.parser()
					.verifyWith(key)
					.build()
					.parseSignedClaims(token)
					.getPayload();
	}
	
	
}
