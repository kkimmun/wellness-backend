package com.kh.wellness.admin.place.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaceImg {

	private Long imgNo;
	private Long placeNo;
	private String originalName;
	private String saveName;
	private String imgPath;
	private Integer imgOrder;
}
