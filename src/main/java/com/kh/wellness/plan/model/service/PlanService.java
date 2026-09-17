package com.kh.wellness.plan.model.service;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;

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
import com.kh.wellness.plan.model.dto.SavedPlanResponseDto;
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
		validateRequest(request);

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

		savePlaces(plan.getPlanNo(), request.getPlaces());

		return plan.getPlanNo();
	}

	public List<PlanResponseDto> findPlans(Long memberNo) {
		return planMapper.findPlansByMember(memberNo);
	}

	public SavedPlanResponseDto findPlan(Long memberNo, Long planNo) {
		Plan plan = requireOwnedPlan(memberNo, planNo);
		return new SavedPlanResponseDto(
				plan.getPlanNo(),
				plan.getPlanName(),
				plan.getXAxis(),
				plan.getYAxis(),
				plan.getCreateDate(),
				planMapper.findPlacesByPlanNo(planNo));
	}

	@Transactional
	public void updatePlan(Long memberNo, Long planNo, PlanCreateRequestDto request) {
		requireOwnedPlan(memberNo, planNo);
		validateRequest(request);
		Plan plan = Plan.builder()
				.planNo(planNo)
				.memberNo(memberNo)
				.planName(request.getPlanName())
				.xAxis(request.getXAxis())
				.yAxis(request.getYAxis())
				.build();
		if (planMapper.updatePlan(plan) == 0) {
			throw new BadRequestException("플랜 수정에 실패하였습니다.");
		}
		planMapper.deletePlanSessions(planNo);
		savePlaces(planNo, request.getPlaces());
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

	private void validateRequest(PlanCreateRequestDto request) {
		if (request == null) {
			throw new BadRequestException("계획 정보를 입력해주세요.");
		}
		String planName = request.getPlanName();
		if (planName == null || planName.isBlank()) {
			throw new BadRequestException("계획 이름을 입력해주세요.");
		}
		if (planName.length() > 30) {
			throw new BadRequestException("계획 이름은 30자 이내로 입력해주세요.");
		}
		if (!isValidLongitude(request.getXAxis()) || !isValidLatitude(request.getYAxis())) {
			throw new BadRequestException("시작 위치 좌표를 확인해주세요.");
		}

		List<PlanPlaceRequestDto> places = request.getPlaces();
		if (places == null || places.isEmpty()) {
			throw new BadRequestException("계획에는 한 개 이상의 장소가 필요합니다.");
		}
		if (places.size() > 10) {
			throw new BadRequestException("계획에는 장소를 최대 10개까지 저장할 수 있습니다.");
		}
		if (places.stream().anyMatch(Objects::isNull)) {
			throw new BadRequestException("장소 정보를 입력해주세요.");
		}

		List<Long> placeNos = places.stream()
				.map(PlanPlaceRequestDto::getPlaceNo)
				.toList();
		if (placeNos.stream().anyMatch(placeNo -> placeNo == null)) {
			throw new BadRequestException("장소 번호를 입력해주세요.");
		}
		if (new HashSet<>(placeNos).size() != placeNos.size()) {
			throw new BadRequestException("같은 장소를 중복해서 저장할 수 없습니다.");
		}
		if (planMapper.countAvailablePlaces(placeNos) != placeNos.size()) {
			throw new BadRequestException("저장할 수 없는 장소가 포함되어 있습니다.");
		}
	}

	private boolean isValidLongitude(Double longitude) {
		return longitude != null && Double.isFinite(longitude)
				&& longitude >= -180 && longitude <= 180;
	}

	private boolean isValidLatitude(Double latitude) {
		return latitude != null && Double.isFinite(latitude)
				&& latitude >= -90 && latitude <= 90;
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

}
