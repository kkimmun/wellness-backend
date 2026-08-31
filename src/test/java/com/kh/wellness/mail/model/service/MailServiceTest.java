package com.kh.wellness.mail.model.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import com.kh.wellness.exception.BadRequestException;
import com.kh.wellness.mail.model.dao.MailMapper;
import com.kh.wellness.mail.model.vo.AuthMail;
import com.kh.wellness.member.model.dto.AuthMailDto;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;

@ExtendWith(MockitoExtension.class)
class MailServiceTest {

	@Mock
	private MailMapper mailMapper;

	@Mock
	private JavaMailSender sender;

	@InjectMocks
	private MailService mailService;

	@BeforeEach
	void setUp() {
		// @Value 로 주입되는 메일 템플릿 필드는 Spring Context 없이 직접 채운다.
		ReflectionTestUtils.setField(mailService, "mailSubject", "[Wellness] 회원가입 인증번호입니다.");
		ReflectionTestUtils.setField(mailService, "mailContext", "인증번호: {authCode}");
	}

	private MimeMessage newMimeMessage() {
		return new MimeMessage((Session) null);
	}

	private AuthMailDto authMailDto() {
		return new AuthMailDto("user@wellness.com", null, null, null);
	}

	@Test
	@DisplayName("인증 메일 발송 성공 시 메일을 전송하고 요청한 주소로 인증코드를 저장한다")
	void sendAuthMail_success() throws Exception {
		when(sender.createMimeMessage()).thenReturn(newMimeMessage());
		when(mailMapper.saveAuthMailCode(any(AuthMail.class))).thenReturn(1);

		mailService.sendAuthMail(authMailDto());

		verify(sender).send(any(MimeMessage.class));

		ArgumentCaptor<AuthMail> captor = ArgumentCaptor.forClass(AuthMail.class);
		verify(mailMapper).saveAuthMailCode(captor.capture());
		AuthMail saved = captor.getValue();
		assertThat(saved.getEmailAddr()).isEqualTo("user@wellness.com");
		assertThat(saved.getAuthYn()).isEqualTo("N");
		assertThat(saved.getAuthCode()).matches("\\d{5}");
	}

	@Test
	@DisplayName("인증코드 저장 결과가 없으면 BadRequestException 을 던진다")
	void sendAuthMail_saveFail() throws Exception {
		when(sender.createMimeMessage()).thenReturn(newMimeMessage());
		when(mailMapper.saveAuthMailCode(any(AuthMail.class))).thenReturn(0);

		assertThatThrownBy(() -> mailService.sendAuthMail(authMailDto()))
				.isInstanceOf(BadRequestException.class)
				.hasMessage("요청에 실패했습니다.");
	}

	@Test
	@DisplayName("인증 메일 재발송 시 기존 인증 정보를 삭제한 뒤 새 인증 메일을 발송한다")
	void resendAuthMail_deletesThenResends() throws Exception {
		AuthMailDto email = authMailDto();
		when(sender.createMimeMessage()).thenReturn(newMimeMessage());
		when(mailMapper.saveAuthMailCode(any(AuthMail.class))).thenReturn(1);

		mailService.resendAuthMail(email);

		InOrder inOrder = Mockito.inOrder(mailMapper);
		inOrder.verify(mailMapper).deleteAuthMail(email);
		inOrder.verify(mailMapper).saveAuthMailCode(any(AuthMail.class));
	}

	@Test
	@DisplayName("이메일 인증 성공 시 인증 완료 처리(삭제)를 수행한다")
	void verifyEmailCode_success() {
		AuthMailDto email = new AuthMailDto("user@wellness.com", 12345, null, null);
		when(mailMapper.verifyEmailTime(email)).thenReturn(1);
		when(mailMapper.verifyEmailCode(email)).thenReturn(1);

		mailService.verifyEmailCode(email);

		verify(mailMapper).deleteSuccessEmail(email);
		verify(mailMapper, never()).deleteExpiredEmail(any());
	}

	@Test
	@DisplayName("인증 시간이 만료되면 만료 이메일을 정리하고 BadRequestException 을 던진다")
	void verifyEmailCode_expired() {
		AuthMailDto email = new AuthMailDto("user@wellness.com", 12345, null, null);
		when(mailMapper.verifyEmailTime(email)).thenReturn(0);

		assertThatThrownBy(() -> mailService.verifyEmailCode(email))
				.isInstanceOf(BadRequestException.class)
				.hasMessage("요청 시간이 만료되었습니다.");

		verify(mailMapper).deleteExpiredEmail(email);
		verify(mailMapper, never()).verifyEmailCode(any());
	}

	@Test
	@DisplayName("인증코드가 일치하지 않으면 BadRequestException 을 던지고 인증 완료 처리를 하지 않는다")
	void verifyEmailCode_codeMismatch() {
		AuthMailDto email = new AuthMailDto("user@wellness.com", 99999, null, null);
		when(mailMapper.verifyEmailTime(email)).thenReturn(1);
		when(mailMapper.verifyEmailCode(email)).thenReturn(0);

		assertThatThrownBy(() -> mailService.verifyEmailCode(email))
				.isInstanceOf(BadRequestException.class)
				.hasMessage("인증에 실패하였습니다.");

		verify(mailMapper, never()).deleteSuccessEmail(any());
	}

	@Test
	@DisplayName("만료 인증 이메일 정리는 예외 없이 수행된다")
	void authMailCleanup_runs() {
		when(mailMapper.authMailCleanup()).thenReturn(3);

		assertThatCode(() -> mailService.authMailCleanup()).doesNotThrowAnyException();

		verify(mailMapper).authMailCleanup();
	}
}
