package com.kh.wellness.review.model.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewImageDto {

	private Long imgNo;
	private Long reviewNo;
	private Integer imgOrder;
	private String imageUrl;
}
