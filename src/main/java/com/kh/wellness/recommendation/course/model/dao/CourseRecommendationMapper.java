package com.kh.wellness.recommendation.course.model.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.kh.wellness.recommendation.course.model.dto.CourseRecommendationCandidateRow;

@Mapper
public interface CourseRecommendationMapper {

    List<CourseRecommendationCandidateRow> findAllCandidates();
}
