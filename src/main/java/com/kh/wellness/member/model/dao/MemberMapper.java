package com.kh.wellness.member.model.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.kh.wellness.member.model.dto.MemberEditValidation;
import com.kh.wellness.member.model.dto.MemberRequestDto;
import com.kh.wellness.member.model.vo.Member;
import com.kh.wellness.member.model.vo.MemberImg;
import com.kh.wellness.member.model.vo.NormalMember;

@Mapper
public interface MemberMapper {

	int countByMemberId(String memberId);

	int insertMember(Member memberEntity);

	MemberRequestDto memberMoreDetails(Long memberNo);

	int userEdit(@Param("memberNo") Long memberNo, @Param("validedMember") MemberEditValidation validedMember);

	int userDelete(Long memberNo);

	MemberImg findById(Long memberNo);

	int signUpNormalMember(NormalMember normalMember);

}
