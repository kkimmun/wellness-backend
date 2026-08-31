package com.kh.wellness.token.model.dao;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import com.kh.wellness.token.model.vo.RefreshToken;

@Mapper
public interface TokenMapper {

	void saveToken(RefreshToken token);

	void deleteToken(Long memberNo);

	RefreshToken findByToken(String token);

}
