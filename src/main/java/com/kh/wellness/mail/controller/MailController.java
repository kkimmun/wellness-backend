package com.kh.wellness.mail.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kh.wellness.common.api.ApiResponse;
import com.kh.wellness.mail.model.service.MailService;
import com.kh.wellness.member.model.dto.AuthMailDto;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/mail")
@RequiredArgsConstructor
public class MailController {
	private final MailService mailService;
		
	@PostMapping("/auth")
	public ResponseEntity<String> sendAuthMail(@RequestBody AuthMailDto emailDto){			
		try {
			mailService.sendAuthMail(emailDto);	
			return ResponseEntity.ok("난수 메일 발송 성공");
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(500).body("난수 생성 실패, 다시 시도해주세요.");
		}		
	}
	
	@PostMapping("/auth/resend")
	public ResponseEntity<String> resendAuthMail(@RequestBody AuthMailDto emailDto){			
		try {
			mailService.resendAuthMail(emailDto);	
			return ResponseEntity.ok("난수 메일 발송 성공");
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(500).body("난수 생성 실패, 다시 시도해주세요.");
		}		
	}
	
	@PostMapping("/auth/verification")
	public ResponseEntity<ApiResponse<Void>> verifyEmailCode(@RequestBody AuthMailDto email){
		
		mailService.verifyEmailCode(email);
		
		return ResponseEntity.status(200).body(ApiResponse.success("인증 성공", null));
	}
	
}
