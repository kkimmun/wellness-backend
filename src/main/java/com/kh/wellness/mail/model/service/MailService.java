package com.kh.wellness.mail.model.service;

import java.util.Random;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kh.wellness.exception.BadRequestException;
import com.kh.wellness.file.service.FileService;
import com.kh.wellness.file.service.S3Service;
import com.kh.wellness.mail.model.dao.MailMapper;
import com.kh.wellness.mail.model.vo.AuthMail;
import com.kh.wellness.member.model.dao.MemberImgMapper;
import com.kh.wellness.member.model.dao.MemberMapper;
import com.kh.wellness.member.model.dto.AuthMailDto;
import com.kh.wellness.member.model.service.MemberService;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MailService {
	private final MailMapper mailMapper;
	private final JavaMailSender sender;
	
		private AuthMail mailTemplate() throws MessagingException {
			Random random = new Random();
			int randomNum = random.nextInt(99999)+1;
			String randomString = String.format("%05d", randomNum);
			
			String to = "koyong3941@gmail.com";
			MimeMessage message = sender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");		
			
			helper.setSubject("패스파인더 회원가입 인증번호입니다.");
			helper.setText(""+randomString+"");		
			helper.setTo(to);
			
			sender.send(message);
			
			AuthMail mailEntity = AuthMail.builder()
					.emailAddr("koyong3941@gmail.com")
					.authCode(randomString)
					.authYn("N")
					.userIp("192.168.51.10")
					.build();
			
			return mailEntity;
		}

		public void sendAuthMail() throws MessagingException {		
			AuthMail mailEntity = mailTemplate();
			
			int result = mailMapper.saveAuthMailCode(mailEntity);
			
			if(result < 1) {
				throw new BadRequestException("요청에 실패했습니다.");
			}
		}

		public void resendAuthMail(AuthMailDto email) throws MessagingException {		
			mailMapper.deleteAuthMail(email);
			
			sendAuthMail();

		}
		
		@Transactional(noRollbackFor = BadRequestException.class)
		public void verifyEmailCode(AuthMailDto email) {
			int resultTime = mailMapper.verifyEmailTime(email);
			
			if(resultTime < 1) {
				deleteExpiredEmail(email);
				throw new BadRequestException("요청 시간이 만료되었습니다.");
			}
			
			int resultCode = mailMapper.verifyEmailCode(email);
			
			if(resultCode < 1) {
				throw new BadRequestException("인증에 실패하였습니다.");
			}
			
			deleteSuccessEmail(email);
		}
			
		// 이메일 만료 시 소프트 삭제
		private void deleteExpiredEmail(AuthMailDto email) {
			mailMapper.deleteExpiredEmail(email);

		}
		
		// 이메일 인증 후 삭제
		private void deleteSuccessEmail(AuthMailDto email) {
			mailMapper.deleteSuccessEmail(email);
		}
		
		//AUTH_Y인 db에 대해 특정 시점 도래 시 하드 딜리트 처리
			
		@Transactional
		@Scheduled(cron = "0 00 03 * * *") 
		public void authMailCleanup() {
			int result = mailMapper.authMailCleanup();
			
			if (result > 0) {
		        log.info("만료된 인증 이메일 정리 완료: {}건 삭제됨", result);
		    } else {
		        log.info("정리할 만료 이메일이 없습니다.");
		    }
			 
		}
			
	}

