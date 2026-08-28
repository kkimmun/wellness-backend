package com.kh.wellness.auth.model.vo;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CustomUserDetails implements UserDetails {
	private Long memberNo;
	private String username; // BASIC → MEMBER_ID / SNS → SOCIAL_ID
	private String password; // BASIC → MEMBER_PWD / SNS → null
	private String memberName; 
	private Collection<? extends GrantedAuthority> authorities;
	private String status;

}
