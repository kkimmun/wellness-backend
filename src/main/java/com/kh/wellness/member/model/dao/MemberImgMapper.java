package com.kh.wellness.member.model.dao;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.kh.wellness.member.model.dto.MemberImgDto;

@Mapper
public interface MemberImgMapper {

	List<MemberImgDto> memberImgCount(Long memberNo);

	Long findMaxCount(Long memberNo);

	void userImgDeleteList(@Param("memberNo") Long memberNo, @Param("imgNo") Long imgNo);

	int userImgUpload(MemberImgDto imgDto);

	void userImgDelete(Long memberNo);

}