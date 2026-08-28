package com.kh.wellness.mail.model.dao;

import org.apache.ibatis.annotations.Mapper;

import com.kh.wellness.mail.model.vo.AuthMail;
import com.kh.wellness.member.model.dto.AuthMailDto;

@Mapper
public interface MailMapper {
	
	int saveAuthMailCode(AuthMail mailEntity);

	void deleteAuthMail(AuthMailDto email);
	
	int verifyEmailCode(AuthMailDto email);

	int verifyEmailTime(AuthMailDto email);

	void deleteSuccessEmail(AuthMailDto email);
	
	void deleteExpiredEmail(AuthMailDto email);

	void verifyExpiredEmail(AuthMailDto email);

	int authMailCleanup();
}
