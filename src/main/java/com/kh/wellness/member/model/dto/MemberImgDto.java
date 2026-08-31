package com.kh.wellness.member.model.dto;

import java.sql.Date;

import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MemberImgDto {
	private Long imgNo;
	private Long memberNo;
	private String originalName;
	private String saveName;
	private String imgPath;
	private Date createDate;
	private String delYn;
	private MultipartFile imageFile;
}