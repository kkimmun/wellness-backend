package com.kh.wellness.review.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewImg {

	private Long imgNo;
	private Long reviewNo;
	private String originalName;
	private String saveName;
	private String imgPath;
	private Integer imgOrder;
}
