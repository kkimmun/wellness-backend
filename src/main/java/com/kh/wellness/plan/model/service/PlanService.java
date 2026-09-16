package com.kh.wellness.plan.model.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kh.wellness.exception.BadRequestException;
import com.kh.wellness.exception.ForbiddenException;
import com.kh.wellness.exception.NotFoundException;
import com.kh.wellness.plan.model.dao.PlanMapper;
import com.kh.wellness.plan.model.dto.PlanCreateRequestDto;
import com.kh.wellness.plan.model.dto.PlanDetailResponse;
import com.kh.wellness.plan.model.dto.PlanPlaceRequestDto;
import com.kh.wellness.plan.model.dto.PlanResponseDto;
import com.kh.wellness.plan.model.vo.Plan;
import com.kh.wellness.plan.model.vo.PlanSession;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlanService {
	private final PlanMapper planMapper;

	@Transactional
	public Long createPlan(Long memberNo, PlanCreateRequestDto request) {

		Plan plan = Plan.builder()
				.memberNo(memberNo)
				.planName(request.getPlanName())
				.xAxis(request.getXAxis())
				.yAxis(request.getYAxis())
				.build();

		int result = planMapper.insertPlan(plan);

		if (result == 0 || plan.getPlanNo() == null) {
			throw new BadRequestException("플랜 생성에 실패하였습니다.");
		}

		return plan.getPlanNo();
	}

	public List<PlanResponseDto> findPlans(Long memberNo) {

		return planMapper.findPlansByMember(memberNo).stream()
				.map(this::toPlanResponseDto)
				.toList();
	}

	@Transactional
	public void addPlaces(Long memberNo, Long planNo, List<PlanPlaceRequestDto> planRequest) {

		requireOwnedPlan(memberNo, planNo);

		savePlaces(planNo, planRequest);
	}

	@Transactional
	public void editPlaces(Long memberNo, Long planNo, List<PlanPlaceRequestDto> planRequest) {

		requireOwnedPlan(memberNo, planNo);
		// planMapper.deletePlanSessions(planNo);

		savePlaces(planNo, planRequest);
	}

	@Transactional
	public void deletePlan(Long memberNo, Long planNo) {

		requireOwnedPlan(memberNo, planNo);

		planMapper.deletePlanSessions(planNo);
		planMapper.deletePlan(planNo);
	}

	public List<PlanDetailResponse> findNearbyPlaces(Long memberNo, Double xAxis, Double yAxis) {

		return planMapper.findNearbyPlaces(memberNo, xAxis, yAxis);
	}

	private void savePlaces(Long planNo, List<PlanPlaceRequestDto> planRequest) {

		for (int i = 0; i < planRequest.size(); i++) {
			PlanSession planSession = PlanSession.builder()
					.planNo(planNo)
					.placeNo(planRequest.get(i).getPlaceNo())
					.placeOrder(i + 1)
					.build();

			int result = planMapper.insertPlanSession(planSession);

			if (result == 0) {
				throw new BadRequestException("저장에 실패하였습니다.");
			}
		}
	}

	private Plan requireOwnedPlan(Long memberNo, Long planNo) {

		Plan plan = planMapper.findPlanForAuth(planNo);

		if (plan == null) {
			throw new NotFoundException("존재하지 않는 플랜입니다.");
		}
		if (!memberNo.equals(plan.getMemberNo())) {
			throw new ForbiddenException("본인의 플랜만 이용할 수 있습니다.");
		}

		return plan;
	}

	private PlanResponseDto toPlanResponseDto(Plan plan) {
		return new PlanResponseDto(
				plan.getPlanNo(),
				plan.getPlanName(),
				plan.getXAxis(),
				plan.getYAxis(),
				plan.getCreateDate());
	}

}
