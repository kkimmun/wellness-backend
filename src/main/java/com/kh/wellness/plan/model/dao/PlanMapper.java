package com.kh.wellness.plan.model.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.kh.wellness.plan.model.dto.PlanDetailResponse;
import com.kh.wellness.plan.model.vo.Plan;
import com.kh.wellness.plan.model.vo.PlanSession;

@Mapper
public interface PlanMapper {

	int insertPlan(Plan plan);

	Plan findPlanForAuth(Long planNo);

	List<Plan> findPlansByMember(Long memberNo);

	int deletePlan(Long planNo);

	int insertPlanSession(PlanSession planSession);

	void deletePlanSessions(Long planNo);

	List<PlanDetailResponse> findNearbyPlaces(@Param("memberNo") Long memberNo, @Param("xAxis")  Double xAxis,
			@Param("yAxis") Double yAxis);

}
