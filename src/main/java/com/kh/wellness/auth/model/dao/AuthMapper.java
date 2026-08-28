package com.kh.wellness.auth.model.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import com.kh.wellness.member.model.dto.MemberDto;

@Mapper
public interface AuthMapper {

	@Select("SELECT MEMBER_NO, MEMBER_ID, MEMBER_PWD, ROLE, DEL_YN FROM MEMBER WHERE DEL_YN ='N' AND MEMBER_ID = #{username}")
	MemberDto loadUser(String username);
	
}

