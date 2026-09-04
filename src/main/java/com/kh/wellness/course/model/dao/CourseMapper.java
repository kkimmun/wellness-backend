package com.kh.wellness.course.model.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.kh.wellness.course.model.dto.CourseListRow;
import com.kh.wellness.course.model.dto.CourseResponse;
import com.kh.wellness.course.model.dto.PlaceDto;
import com.kh.wellness.course.model.dto.WaypointDto;
import com.kh.wellness.course.model.enums.CourseTag;

@Mapper
public interface CourseMapper {

    List<CourseListRow> selectActiveCourses(
            @Param("offset") long offset,
            @Param("size") int size);

    long countActiveCourses();

    List<PlaceDto> selectRestaurants();
    
    CourseResponse selectByCourseNo(Long courseNo);

	List<WaypointDto> selectWaypointBycourseNo(Long courseNo);

	List<PlaceDto> selectByTags(@Param(value="tags") List<CourseTag> tags);
    
    
}
