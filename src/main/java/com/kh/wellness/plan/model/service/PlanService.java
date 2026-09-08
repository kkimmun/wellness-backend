package com.kh.wellness.plan.model.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kh.wellness.exception.BadRequestException;
import com.kh.wellness.plan.model.dao.PlanMapper;
import com.kh.wellness.plan.model.dto.PlanDetailResponse;
import com.kh.wellness.plan.model.dto.PlanPlaceRequestDto;
import com.kh.wellness.plan.model.vo.Plan;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlanService {
	private final PlanMapper planMapper;

	@Transactional
	public void savePlan(Long memberNo, List<PlanPlaceRequestDto> planRequest) {
		
		for (int i = 0; i < planRequest.size(); i++) {
		    Plan planEntity = Plan.builder()
		            .memberNo(memberNo)
		            .placeNo(planRequest.get(i).getPlaceNo())
		            .placeOrder(i + 1)
		            .build();
		    
		int result = planMapper.savePlan(planEntity);
		    
			
			if(result == 0) {
				throw new BadRequestException("저장에 실패하였습니다.");
			}
		
		}
		
	}

	@Transactional
	public void editPlan(Long memberNo, List<PlanPlaceRequestDto> planRequest) {

	    deletePlan(memberNo);

	    savePlan(memberNo, planRequest);
	}

	public void deletePlan(Long memberNo) {
		
		planMapper.deletePlan(memberNo);
		
	}

	public List<PlanDetailResponse> findNearbyPlaces(Long memberNo, Double xAxis, Double yAxis, Integer radius) {
		
		return planMapper.findNearbyPlaces(memberNo, xAxis, yAxis, radius);
	}
	
}
