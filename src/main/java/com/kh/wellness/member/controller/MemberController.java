package com.kh.wellness.member.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kh.wellness.auth.model.vo.CustomUserDetails;
import com.kh.wellness.common.api.ApiResponse;
import com.kh.wellness.member.model.dto.AuthMailDto;
import com.kh.wellness.member.model.dto.MemberDto;
import com.kh.wellness.member.model.dto.MemberEditValidation;
import com.kh.wellness.member.model.dto.MemberImgDto;
import com.kh.wellness.member.model.dto.MemberRequestDto;
import com.kh.wellness.member.model.service.MemberService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequestMapping("/api/members")
@RestController
@RequiredArgsConstructor
public class MemberController {
	private final MemberService memberService;
	
	// 유저 생성
	@PostMapping
	public ResponseEntity<ApiResponse<String>> signUp(@Valid @RequestBody MemberDto member){
		memberService.signUp(member);
		String message = "유저 회원가입 성공";
	return ResponseEntity.status(201).body(ApiResponse.created("가입 성공", message));
	}
	
	// 유저 정보 추가 조회
	@GetMapping("/detail")
	public ResponseEntity<ApiResponse<MemberRequestDto>> memberMoreDetails(@AuthenticationPrincipal CustomUserDetails userDetails){
		
		Long memberNoFromToken = userDetails.getMemberNo();
		MemberRequestDto memberInfo = memberService.memberMoreDetails(memberNoFromToken);
		return ResponseEntity.status(200).body(ApiResponse.success("조회 성공", memberInfo));
	}
	
	// 수정
	@PatchMapping("/edit")
	public ResponseEntity<ApiResponse<Void>> userEdit(@AuthenticationPrincipal CustomUserDetails userDetails, @Valid @RequestBody MemberEditValidation validedMember){
		Long memberNoFromToken = userDetails.getMemberNo();
		memberService.userEdit(memberNoFromToken, validedMember);
		return ResponseEntity.status(200).body(ApiResponse.success("수정 성공", null));
	}
	
	// 삭제
	@DeleteMapping
	public ResponseEntity<ApiResponse<Void>> userDelete(@AuthenticationPrincipal CustomUserDetails userDetails){
		Long memberNoFromToken = userDetails.getMemberNo();
		memberService.userDelete(memberNoFromToken);
		return ResponseEntity.status(200).body(ApiResponse.success("삭제 성공", null));
	}
	
	// 유저 이미지 업로드
	@PostMapping("/profile/image")
	public ResponseEntity<ApiResponse<MemberImgDto>> userImgUpload(@AuthenticationPrincipal CustomUserDetails userDetails,
													   @ModelAttribute MemberImgDto member){
		Long memberNoFromToken = userDetails.getMemberNo();
		 
		member = memberService.userImgUpload(memberNoFromToken, member.getImageFile());
		
		return ResponseEntity.status(201).body(ApiResponse.created("이미지 업로드 성공",member));
	}
	// 유저 이미지 업로드 삭제
	@DeleteMapping("/profile/image")
	public ResponseEntity<ApiResponse<Void>> userImgDelete(@AuthenticationPrincipal CustomUserDetails userDetails){
		Long memberNoFromToken = userDetails.getMemberNo();
		memberService.userImgDelete(memberNoFromToken);
		return ResponseEntity.status(204).body(ApiResponse.noContent("삭제 성공", null));
	}
	
	
}
