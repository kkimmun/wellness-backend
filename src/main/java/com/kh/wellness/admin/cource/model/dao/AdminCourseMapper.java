package com.kh.wellness.admin.cource.model.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.kh.wellness.admin.cource.model.dto.AdminCourseListResponse;
import com.kh.wellness.admin.cource.model.vo.Course;
import com.kh.wellness.admin.cource.model.vo.CourseWaypoint;

@Mapper
public interface AdminCourseMapper {

	List<AdminCourseListResponse> selectCourses(@Param("keyword") String keyword, @Param("active") String active,
			@Param("offset") long offset, @Param("size") int size);

	long countCourses(@Param("keyword") String keyword, @Param("active") String active);

	int countExistingPlaces(List<Long> placeNos);

	int insertCourse(Course course);

	int insertCourseWaypoint(CourseWaypoint waypoint);

	int countCourseByNo(Long courseNo);

	int updateCourse(Course course);

	int deleteCourseWaypoints(Long courseNo);

	int deleteCourse(Long courseNo);

	int updateCourseStatus(@Param("courseNo") Long courseNo, @Param("active") String active);
}
