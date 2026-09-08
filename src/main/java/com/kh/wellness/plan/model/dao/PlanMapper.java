package com.kh.wellness.plan.model.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.kh.wellness.plan.model.dto.PlanDetailResponse;
import com.kh.wellness.plan.model.vo.Plan;

@Mapper
public interface PlanMapper {

	int savePlan(Plan planEntity);

	void deletePlan(Long memberNo);

	List<PlanDetailResponse> findNearbyPlaces(@Param("memberNo") Long memberNo, @Param("xAxis")  Double xAxis, 
			@Param("yAxis") Double yAxis);

}
