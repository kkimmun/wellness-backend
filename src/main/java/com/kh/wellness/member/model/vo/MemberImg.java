package com.kh.wellness.member.model.vo;

import java.sql.Date;

import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class MemberImg {
	private Long imgNo;
	private Long memberNo;
	private String originalName;
	private String saveName;
	private String imgPath;
	private Date createDate;
	private String delYn;
	private MultipartFile imageFile;
}