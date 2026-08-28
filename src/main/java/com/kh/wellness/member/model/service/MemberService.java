package com.kh.wellness.member.model.service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.kh.wellness.exception.BadRequestException;
import com.kh.wellness.exception.ConflictException;
import com.kh.wellness.exception.FileException;
import com.kh.wellness.exception.NotFoundException;
import com.kh.wellness.file.dto.FileSaveResult;
import com.kh.wellness.file.service.FileService;
import com.kh.wellness.file.service.S3Service;
import com.kh.wellness.member.model.dao.MemberImgMapper;
import com.kh.wellness.member.model.dao.MemberMapper;
import com.kh.wellness.member.model.dto.MemberDto;
import com.kh.wellness.member.model.dto.MemberEditValidation;
import com.kh.wellness.member.model.dto.MemberImgDto;
import com.kh.wellness.member.model.dto.MemberRequestDto;
import com.kh.wellness.member.model.vo.Member;
import com.kh.wellness.member.model.vo.MemberImg;
import com.kh.wellness.member.model.vo.NormalMember;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {
	private final MemberMapper memberMapper;
	private final MemberImgMapper memberImgMapper;
	private final FileService fileService;
	private final PasswordEncoder passwordEncoder;
	private final S3Service s3Service;
	
	//멤버 등록
	@Transactional
	public void signUp(MemberDto member) {
		countByMemberId(member.getMemberId());
		Member memberEntity = Member.builder()
				.memberName(member.getMemberName())
				.role("USER")
				.socialProvider("BASIC")
				.delYn("N")
				.build();
		
		int result = memberMapper.insertMember(memberEntity);
		
		if (result != 1) {
			throw new BadRequestException("회원가입에 실패했습니다.");
		}
		
		NormalMember normalMember = NormalMember.builder()
				.memberNo(memberEntity.getMemberNo())
				.memberId(member.getMemberId())
				.memberPwd(passwordEncoder.encode(member.getMemberPwd()))
				.build();
		
		result = memberMapper.signUpNormalMember(normalMember);
		if (result != 1) {
			throw new BadRequestException("회원가입에 실패했습니다.");
			
		}
	}

	private void countByMemberId(String memberId) { //.
		int result = memberMapper.countByMemberId(memberId);
		if(result > 0 ) { 
			throw new ConflictException ("등록된 아이디가 존재합니다.");
		}
	}

	// 멤버 상세조회 *앞단에서 로그인 시 추가 정보를 페이로드에 담지 않고 스토리지 저장함
	public MemberRequestDto memberMoreDetails(Long memberNo) {
		MemberRequestDto  requestResult = memberMapper.memberMoreDetails(memberNo);
		
		if(requestResult == null) {
			throw new NotFoundException("사용자 정보 요청에  실패하였습니다.");
		}
		
		String delYn = requestResult.getDelYn();
		
		requestResult.setDelYn(
				"N".equals(delYn)? "N":"Y"
				);
		
		return requestResult;
	}
	
	// 멤버 수정
	@Transactional
	public int userEdit(Long memberNo, MemberEditValidation validedMember) {		
		validedMember.setMemberPwd(passwordEncoder.encode(validedMember.getMemberPwd()));
		
		int memberEditedResult = memberMapper.userEdit(memberNo, validedMember);
		
		if(memberEditedResult < 1){
			throw new BadRequestException("사용자 수정에  실패하였습니다, 다시 시도해주세요.");
		}
		
		return memberEditedResult;
	}
	
	// 멤버 삭제
	@Transactional
	public void userDelete(Long memberNo) {
		int memberDeletedResult = memberMapper.userDelete(memberNo);

		if(memberDeletedResult < 1){
			throw new BadRequestException("사용자 수정에  실패하였습니다, 다시 시도해주세요.");
		}
	}
	
	// 멤버 이미지 업로드
	@Transactional 
	public MemberImgDto userImgUpload(Long memberNo, MultipartFile imageFile) {
		// 이미지 조회 조회 시 delYn n이 여러개 일 경우 낮은 imgNo를 가진 이미지 삭제(userImgDelete 메서드 재활용)
		//만약 이미지 조회 시 2개 이상인 경우 imgNo가 낮은 번호를 del_yn Y로 변경
		
		if (imageFile == null || imageFile.isEmpty()) {
			throw new FileException("사용자 이미지 파일 업로드에  실패하였습니다, 다시 시도해주세요.");
		}
		
		FileSaveResult result = fileService.store(imageFile, "profile");
		
		try {
		MemberImgDto imgDto = new MemberImgDto();
        imgDto.setMemberNo(memberNo);
        imgDto.setOriginalName(imageFile.getOriginalFilename());
        imgDto.setSaveName(result.getSaveName());
        imgDto.setImgPath(result.getImgPath());
		
        memberImgMapper.userImgUpload(imgDto);
        
        removeDuplicateImages(memberNo);
        
		return imgDto;
		} catch (Exception e) {
			throw new FileException("저장에 실패했습니다.");
		}
	}
	
	private void removeDuplicateImages(Long memberNo) {
		List<MemberImgDto> list = memberImgMapper.memberImgCount(memberNo);
		//기존 /*list.size() > 1*/
		if( list.size() > 1){
			imageDupleDelete(memberNo, list);
		}
	}
	
	private void imageDupleDelete(Long memberNo, List<MemberImgDto> list) {
		Long imageList = memberImgMapper.findMaxCount(memberNo);
		// 가져온게 여러행일 경우*활성화 이미지가 중복, 가장 최근 이미지말고 다 Del_yn = Y업데이트 치게 구현
		
		for(MemberImgDto memberImg : list){
			if(memberImg.getImgNo().equals(imageList)) {
				continue;
			}	
			memberImgMapper.userImgDeleteList(memberNo, memberImg.getImgNo());
		} 
	}
	
	// 멤버 이미지 삭제
	@Transactional
	public void userImgDelete(Long memberNo) {
		
		MemberImg entity = memberMapper.findById(memberNo);
		
		if(entity != null && entity.getImgPath() != null && !entity.getImgPath().isEmpty()) {
			s3Service.deleteFile(entity.getImgPath());
		}
		
		memberImgMapper.userImgDelete(memberNo);
	}

	

}
