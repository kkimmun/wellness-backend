package com.kh.wellness.plan.model.dao;

import org.apache.ibatis.annotations.Mapper;

import com.kh.wellness.plan.model.vo.Plan;

@Mapper
public interface PlanMapper {

	int savePlan(Plan planEntity);

	void deletePlan(Long memberNo);

}
