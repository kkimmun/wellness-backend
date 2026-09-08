package com.kh.wellness.review.model.vo;

import java.sql.Timestamp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Review {

	private Long reviewNo;
	private Long memberNo;
	private Long placeNo;
	private String reviewContent;
	private Integer rating;
	private Timestamp createDate;
	private String delYn;
	private Timestamp deleteDate;
}
