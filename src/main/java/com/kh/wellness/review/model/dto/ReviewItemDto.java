package com.kh.wellness.review.model.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewItemDto {

	private Long reviewNo;
	private Long memberNo;
	private String nickname;
	private String profileImgPath;
	private Integer rating;
	private String reviewContent;
	private String createDate;
	private List<String> images = new ArrayList<>();
}
