package com.kh.wellness.course.model.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.kh.wellness.course.model.dto.CourseListRow;
import com.kh.wellness.course.model.dto.CourseResponse;

@Mapper
public interface CourseMapper {

    List<CourseListRow> selectActiveCourses(
            @Param("offset") long offset,
            @Param("size") int size);

    long countActiveCourses();
    
    CourseResponse selectByCourseNo(Long courseNo);
}
