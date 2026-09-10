package com.kh.wellness.member.model.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.kh.wellness.member.model.dto.MemberProfileResponse;

@Mapper
public interface MemberProfileMapper {
    MemberProfileResponse findProfile(Long memberNo);
    MemberProfileResponse findProfileForUpdate(Long memberNo);
    int updateName(@Param("memberNo") Long memberNo, @Param("memberName") String memberName);
    String findPassword(Long memberNo);
    int updatePassword(@Param("memberNo") Long memberNo, @Param("oldHash") String oldHash,
                       @Param("newHash") String newHash);
    int updatePhoto(@Param("memberNo") Long memberNo, @Param("originalName") String originalName,
                    @Param("saveName") String saveName, @Param("imgPath") String imgPath);
}
