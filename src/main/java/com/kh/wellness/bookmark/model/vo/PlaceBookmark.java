package com.kh.wellness.bookmark.model.vo;

import java.sql.Timestamp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * PLACE_BOOKMARK (회원 × 장소 북마크).
 * (MEMBER_NO, PLACE_NO) 복합 PK 를 식별자로 사용하며 별도 대체 키는 두지 않는다.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaceBookmark {

	private Long memberNo;
	private Long placeNo;
	private Timestamp createDate;
}
