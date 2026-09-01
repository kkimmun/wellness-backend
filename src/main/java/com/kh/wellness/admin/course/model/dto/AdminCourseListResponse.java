package com.kh.wellness.admin.course.model.dto;

import java.sql.Date;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdminCourseListResponse {
	private Long courseNo;
	private String courseName;
	private Integer estimatedTime;
	private String description;
	private Date createdDate;
	private String active;
}
