package com.kh.wellness.auth.model.dao;

import org.apache.ibatis.annotations.Mapper;

import com.kh.wellness.member.model.dto.MemberDto;

@Mapper
public interface AuthMapper {

	MemberDto loadUserByMemberId(String memberId);

	MemberDto loadUserByMemberNo(Long memberNo);

}

