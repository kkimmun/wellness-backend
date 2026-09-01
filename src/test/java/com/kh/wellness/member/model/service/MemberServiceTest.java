package com.kh.wellness.member.model.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
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
import com.kh.wellness.member.model.vo.MemberImg;
import com.kh.wellness.member.model.vo.NormalMember;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

	@Mock
	private MemberMapper memberMapper;

	@Mock
	private MemberImgMapper memberImgMapper;

	@Mock
	private FileService fileService;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private S3Service s3Service;

	@InjectMocks
	private MemberService memberService;

	private MemberDto signUpDto() {
		MemberDto dto = new MemberDto();
		dto.setMemberId("new@wellness.com");
		dto.setMemberPwd("rawPwd1");
		dto.setMemberName("newbie");
		return dto;
	}

	// ---------- signUp ----------

	@Test
	@DisplayName("회원가입 성공 시 암호화된 비밀번호로 일반회원 정보를 저장한다")
	void signUp_success() {
		MemberDto dto = signUpDto();
		when(memberMapper.countByMemberId("new@wellness.com")).thenReturn(0);
		when(memberMapper.insertMember(any())).thenReturn(1);
		when(passwordEncoder.encode("rawPwd1")).thenReturn("ENCODED");
		when(memberMapper.signUpNormalMember(any())).thenReturn(1);

		memberService.signUp(dto);

		ArgumentCaptor<NormalMember> captor = ArgumentCaptor.forClass(NormalMember.class);
		verify(memberMapper).signUpNormalMember(captor.capture());
		assertThat(captor.getValue().getMemberId()).isEqualTo("new@wellness.com");
		assertThat(captor.getValue().getMemberPwd()).isEqualTo("ENCODED");
	}

	@Test
	@DisplayName("이미 존재하는 아이디면 ConflictException 을 던지고 회원 정보를 저장하지 않는다")
	void signUp_duplicateId() {
		MemberDto dto = signUpDto();
		when(memberMapper.countByMemberId("new@wellness.com")).thenReturn(1);

		assertThatThrownBy(() -> memberService.signUp(dto))
				.isInstanceOf(ConflictException.class)
				.hasMessage("등록된 아이디가 존재합니다.");

		verify(memberMapper, never()).insertMember(any());
		verify(memberMapper, never()).signUpNormalMember(any());
	}

	@Test
	@DisplayName("회원 기본정보 저장에 실패하면 BadRequestException 을 던진다")
	void signUp_insertMemberFail() {
		MemberDto dto = signUpDto();
		when(memberMapper.countByMemberId("new@wellness.com")).thenReturn(0);
		when(memberMapper.insertMember(any())).thenReturn(0);

		assertThatThrownBy(() -> memberService.signUp(dto))
				.isInstanceOf(BadRequestException.class)
				.hasMessage("회원가입에 실패했습니다.");

		verify(memberMapper, never()).signUpNormalMember(any());
	}

	@Test
	@DisplayName("일반회원 정보 저장에 실패하면 BadRequestException 을 던진다")
	void signUp_normalMemberFail() {
		MemberDto dto = signUpDto();
		when(memberMapper.countByMemberId("new@wellness.com")).thenReturn(0);
		when(memberMapper.insertMember(any())).thenReturn(1);
		when(passwordEncoder.encode("rawPwd1")).thenReturn("ENCODED");
		when(memberMapper.signUpNormalMember(any())).thenReturn(0);

		assertThatThrownBy(() -> memberService.signUp(dto))
				.isInstanceOf(BadRequestException.class)
				.hasMessage("회원가입에 실패했습니다.");
	}

	// ---------- memberMoreDetails ----------

	@Test
	@DisplayName("회원 상세 조회 결과가 없으면 NotFoundException 을 던진다")
	void memberMoreDetails_notFound() {
		when(memberMapper.memberMoreDetails(1L)).thenReturn(null);

		assertThatThrownBy(() -> memberService.memberMoreDetails(1L))
				.isInstanceOf(NotFoundException.class)
				.hasMessage("사용자 정보 요청에  실패하였습니다.");
	}

	@Test
	@DisplayName("삭제되지 않은 회원은 delYn 이 N 으로 유지된다")
	void memberMoreDetails_activeMember() {
		MemberRequestDto dto = new MemberRequestDto();
		dto.setDelYn("N");
		when(memberMapper.memberMoreDetails(1L)).thenReturn(dto);

		MemberRequestDto result = memberService.memberMoreDetails(1L);

		assertThat(result.getDelYn()).isEqualTo("N");
	}

	@Test
	@DisplayName("delYn 이 N 이 아니면 Y 로 정규화된다")
	void memberMoreDetails_normalizesDeletedFlag() {
		MemberRequestDto dto = new MemberRequestDto();
		dto.setDelYn(null);
		when(memberMapper.memberMoreDetails(1L)).thenReturn(dto);

		MemberRequestDto result = memberService.memberMoreDetails(1L);

		assertThat(result.getDelYn()).isEqualTo("Y");
	}

	// ---------- userEdit ----------

	@Test
	@DisplayName("회원 수정 성공 시 암호화된 비밀번호로 수정하고 반영 건수를 반환한다")
	void userEdit_success() {
		MemberEditValidation validation = new MemberEditValidation();
		validation.setMemberPwd("rawPwd1");
		when(passwordEncoder.encode("rawPwd1")).thenReturn("ENCODED");
		when(memberMapper.userEdit(eq(1L), any())).thenReturn(1);

		int result = memberService.userEdit(1L, validation);

		assertThat(result).isEqualTo(1);
		assertThat(validation.getMemberPwd()).isEqualTo("ENCODED");
	}

	@Test
	@DisplayName("회원 수정 반영 건수가 없으면 BadRequestException 을 던진다")
	void userEdit_fail() {
		MemberEditValidation validation = new MemberEditValidation();
		validation.setMemberPwd("rawPwd1");
		when(passwordEncoder.encode("rawPwd1")).thenReturn("ENCODED");
		when(memberMapper.userEdit(eq(1L), any())).thenReturn(0);

		assertThatThrownBy(() -> memberService.userEdit(1L, validation))
				.isInstanceOf(BadRequestException.class);
	}

	// ---------- userDelete ----------

	@Test
	@DisplayName("회원 삭제 성공 시 예외가 발생하지 않는다")
	void userDelete_success() {
		when(memberMapper.userDelete(1L)).thenReturn(1);

		assertThatCode(() -> memberService.userDelete(1L)).doesNotThrowAnyException();
	}

	@Test
	@DisplayName("회원 삭제 반영 건수가 없으면 BadRequestException 을 던진다")
	void userDelete_fail() {
		when(memberMapper.userDelete(1L)).thenReturn(0);

		assertThatThrownBy(() -> memberService.userDelete(1L))
				.isInstanceOf(BadRequestException.class);
	}

	// ---------- userImgUpload ----------

	@Test
	@DisplayName("이미지 파일이 없으면 FileException 을 던지고 파일 저장을 시도하지 않는다")
	void userImgUpload_nullFile() {
		assertThatThrownBy(() -> memberService.userImgUpload(1L, null))
				.isInstanceOf(FileException.class);

		verify(fileService, never()).store(any(), any());
	}

	@Test
	@DisplayName("빈 이미지 파일이면 FileException 을 던진다")
	void userImgUpload_emptyFile() {
		MultipartFile empty = new MockMultipartFile("imageFile", new byte[0]);

		assertThatThrownBy(() -> memberService.userImgUpload(1L, empty))
				.isInstanceOf(FileException.class);
	}

	@Test
	@DisplayName("이미지 업로드 성공 시 저장 결과를 담은 DTO 를 반환한다")
	void userImgUpload_success() {
		MultipartFile file = new MockMultipartFile("imageFile", "profile.jpg", "image/jpeg", "img".getBytes());
		when(fileService.store(file, "profile")).thenReturn(new FileSaveResult("saved.jpg", "https://bucket/profile/"));
		when(memberImgMapper.memberImgCount(1L)).thenReturn(List.of(new MemberImgDto()));

		MemberImgDto result = memberService.userImgUpload(1L, file);

		assertThat(result.getMemberNo()).isEqualTo(1L);
		assertThat(result.getOriginalName()).isEqualTo("profile.jpg");
		assertThat(result.getSaveName()).isEqualTo("saved.jpg");
		assertThat(result.getImgPath()).isEqualTo("https://bucket/profile/");
		verify(memberImgMapper).userImgUpload(any(MemberImgDto.class));
	}

	@Test
	@DisplayName("활성 이미지가 2개 이상이면 최신 이미지를 제외한 나머지를 삭제 처리한다")
	void userImgUpload_removesDuplicateImages() {
		MultipartFile file = new MockMultipartFile("imageFile", "profile.jpg", "image/jpeg", "img".getBytes());
		when(fileService.store(file, "profile")).thenReturn(new FileSaveResult("saved.jpg", "https://bucket/profile/"));

		MemberImgDto older = new MemberImgDto();
		older.setImgNo(10L);
		MemberImgDto latest = new MemberImgDto();
		latest.setImgNo(20L);
		when(memberImgMapper.memberImgCount(1L)).thenReturn(List.of(older, latest));
		when(memberImgMapper.findMaxCount(1L)).thenReturn(20L);

		memberService.userImgUpload(1L, file);

		verify(memberImgMapper).userImgDeleteList(1L, 10L);
		verify(memberImgMapper, never()).userImgDeleteList(1L, 20L);
	}

	// ---------- userImgDelete ----------

	@Test
	@DisplayName("저장된 이미지 경로가 있으면 S3 파일을 삭제한 뒤 이미지 정보를 삭제한다")
	void userImgDelete_withStoredImage() {
		MemberImg entity = MemberImg.builder().imgPath("profile/saved.jpg").build();
		when(memberMapper.findById(1L)).thenReturn(entity);

		memberService.userImgDelete(1L);

		verify(s3Service).deleteFile("profile/saved.jpg");
		verify(memberImgMapper).userImgDelete(1L);
	}

	@Test
	@DisplayName("저장된 이미지가 없으면 S3 삭제 없이 이미지 정보만 삭제한다")
	void userImgDelete_withoutStoredImage() {
		when(memberMapper.findById(1L)).thenReturn(null);

		memberService.userImgDelete(1L);

		verify(s3Service, never()).deleteFile(any());
		verify(memberImgMapper).userImgDelete(1L);
	}
}
