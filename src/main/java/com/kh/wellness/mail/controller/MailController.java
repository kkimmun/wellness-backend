package com.kh.wellness.mail.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kh.wellness.common.api.ApiResponse;
import com.kh.wellness.mail.model.service.MailService;
import com.kh.wellness.member.model.dto.AuthMailDto;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/mail")
@RequiredArgsConstructor
public class MailController {
	private final MailService mailService;
		
	@PostMapping("/auth")
	public ResponseEntity<ApiResponse<Void>> sendAuthMail(@RequestBody AuthMailDto emailDto)
			throws MessagingException {
		mailService.sendAuthMail(emailDto);
		return ResponseEntity.ok(ApiResponse.success("인증 메일 발송 성공", null));
	}
	
	@PostMapping("/auth/resend")
	public ResponseEntity<ApiResponse<Void>> resendAuthMail(@RequestBody AuthMailDto emailDto)
			throws MessagingException {
		mailService.resendAuthMail(emailDto);
		return ResponseEntity.ok(ApiResponse.success("인증 메일 재발송 성공", null));
	}
	
	@PostMapping("/auth/verification")
	public ResponseEntity<ApiResponse<Void>> verifyEmailCode(@RequestBody AuthMailDto email){
		
		mailService.verifyEmailCode(email);
		
		return ResponseEntity.status(200).body(ApiResponse.success("인증 성공", null));
	}
	
}
