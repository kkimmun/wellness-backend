package com.kh.wellness.plan.model.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.kh.wellness.exception.BadRequestException;
import com.kh.wellness.exception.ForbiddenException;
import com.kh.wellness.exception.NotFoundException;
import com.kh.wellness.plan.model.dao.PlanMapper;
import com.kh.wellness.plan.model.dto.PlanCreateRequestDto;
import com.kh.wellness.plan.model.dto.PlanPlaceRequestDto;
import com.kh.wellness.plan.model.dto.PlanPlaceResponseDto;
import com.kh.wellness.plan.model.dto.PlanResponseDto;
import com.kh.wellness.plan.model.dto.SavedPlanResponseDto;
import com.kh.wellness.plan.model.vo.Plan;
import com.kh.wellness.plan.model.vo.PlanSession;

@ExtendWith(MockitoExtension.class)
class PlanServiceTest {

    @Mock
    private PlanMapper planMapper;

    private PlanService planService;

    @BeforeEach
    void setUp() {
        planService = new PlanService(planMapper);
    }

    @Test
    void createPlan은_생성된_planNo를_반환한다() throws Exception {
        when(planMapper.insertPlan(any(Plan.class))).thenAnswer(invocation -> {
            Plan plan = invocation.getArgument(0);
            setPlanNo(plan, 10L);
            return 1;
        });

        Long planNo = planService.createPlan(100L, new PlanCreateRequestDto("여행", 127.0, 37.0));

        assertThat(planNo).isEqualTo(10L);
    }

    @Test
    void createPlan은_Mapper가_0건_반영을_반환하면_BadRequestException을_던진다() {
        when(planMapper.insertPlan(any(Plan.class))).thenReturn(0);

        assertThatThrownBy(() -> planService.createPlan(100L, new PlanCreateRequestDto(null, null, null)))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void findPlans는_Mapper가_조회한_목록_응답을_반환한다() {
        PlanResponseDto plan = new PlanResponseDto(10L, "김포 여행", 126.6, 37.6, null, 2);
        when(planMapper.findPlansByMember(100L)).thenReturn(List.of(plan));

        assertThat(planService.findPlans(100L)).containsExactly(plan);
    }

    @Test
    void addPlaces는_본인_소유_플랜이_아니면_ForbiddenException을_던진다() {
        when(planMapper.findPlanForAuth(10L)).thenReturn(ownedPlan(10L, 999L));

        assertThatThrownBy(() -> planService.addPlaces(100L, 10L, List.of(requestDto(11L))))
                .isInstanceOf(ForbiddenException.class);

        verify(planMapper, never()).insertPlanSession(any());
    }

    @Test
    void addPlaces는_존재하지_않는_플랜이면_NotFoundException을_던진다() {
        when(planMapper.findPlanForAuth(10L)).thenReturn(null);

        assertThatThrownBy(() -> planService.addPlaces(100L, 10L, List.of(requestDto(11L))))
                .isInstanceOf(NotFoundException.class);

        verify(planMapper, never()).insertPlanSession(any());
    }

    @Test
    void addPlaces는_요청한_장소들을_입력_순서대로_저장하고_순번을_1부터_부여한다() {
        when(planMapper.findPlanForAuth(10L)).thenReturn(ownedPlan(10L, 100L));
        when(planMapper.insertPlanSession(any(PlanSession.class))).thenReturn(1);

        planService.addPlaces(100L, 10L, List.of(
                requestDto(11L),
                requestDto(22L),
                requestDto(33L)));

        ArgumentCaptor<PlanSession> captor = ArgumentCaptor.forClass(PlanSession.class);
        verify(planMapper, times(3)).insertPlanSession(captor.capture());
        assertThat(captor.getAllValues())
                .extracting(PlanSession::getPlanNo, PlanSession::getPlaceNo, PlanSession::getPlaceOrder)
                .containsExactly(
                        tuple(10L, 11L, 1),
                        tuple(10L, 22L, 2),
                        tuple(10L, 33L, 3));
    }

    @Test
    void addPlaces는_Mapper가_0건_반영을_반환하면_BadRequestException을_던진다() {
        when(planMapper.findPlanForAuth(10L)).thenReturn(ownedPlan(10L, 100L));
        when(planMapper.insertPlanSession(any(PlanSession.class))).thenReturn(0);

        assertThatThrownBy(() -> planService.addPlaces(100L, 10L, List.of(requestDto(11L))))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("저장에 실패하였습니다.");
    }

    @Test
    void editPlaces는_기존_장소를_삭제하고_요청한_장소를_순서대로_저장한다() {
        when(planMapper.findPlanForAuth(10L)).thenReturn(ownedPlan(10L, 100L));
        when(planMapper.insertPlanSession(any(PlanSession.class))).thenReturn(1);

        planService.editPlaces(100L, 10L, List.of(requestDto(11L), requestDto(22L)));

        verify(planMapper).deletePlanSessions(10L);
        ArgumentCaptor<PlanSession> captor = ArgumentCaptor.forClass(PlanSession.class);
        verify(planMapper, times(2)).insertPlanSession(captor.capture());
        assertThat(captor.getAllValues())
                .extracting(PlanSession::getPlanNo, PlanSession::getPlaceNo, PlanSession::getPlaceOrder)
                .containsExactly(tuple(10L, 11L, 1), tuple(10L, 22L, 2));
    }

    @Test
    void findPlan은_본인_계획의_헤더와_장소를_반환한다() {
        Plan plan = Plan.builder()
                .planNo(10L)
                .memberNo(100L)
                .planName("김포 여행")
                .xAxis(126.6)
                .yAxis(37.6)
                .build();
        PlanPlaceResponseDto place = new PlanPlaceResponseDto(
                11L, 1, "장소", "설명", "주소", null,
                126.61, 37.61, 1L, 46L, null, null);
        when(planMapper.findPlanForAuth(10L)).thenReturn(plan);
        when(planMapper.findPlacesByPlanNo(10L)).thenReturn(List.of(place));

        SavedPlanResponseDto result = planService.findPlan(100L, 10L);

        assertThat(result.getPlanNo()).isEqualTo(10L);
        assertThat(result.getPlanName()).isEqualTo("김포 여행");
        assertThat(result.getPlaces()).containsExactly(place);
    }

    @Test
    void updatePlan은_본인_계획의_이름과_시작좌표를_수정한다() {
        when(planMapper.findPlanForAuth(10L)).thenReturn(ownedPlan(10L, 100L));
        when(planMapper.updatePlan(any(Plan.class))).thenReturn(1);

        planService.updatePlan(100L, 10L, new PlanCreateRequestDto("수정 계획", 126.7, 37.7));

        ArgumentCaptor<Plan> captor = ArgumentCaptor.forClass(Plan.class);
        verify(planMapper).updatePlan(captor.capture());
        assertThat(captor.getValue().getPlanNo()).isEqualTo(10L);
        assertThat(captor.getValue().getPlanName()).isEqualTo("수정 계획");
        assertThat(captor.getValue().getXAxis()).isEqualTo(126.7);
        assertThat(captor.getValue().getYAxis()).isEqualTo(37.7);
    }

    @Test
    void deletePlan은_본인_소유_플랜이_아니면_ForbiddenException을_던지고_삭제하지_않는다() {
        when(planMapper.findPlanForAuth(10L)).thenReturn(ownedPlan(10L, 999L));

        assertThatThrownBy(() -> planService.deletePlan(100L, 10L))
                .isInstanceOf(ForbiddenException.class);

        verifyNoInteractions_delete();
    }

    @Test
    void deletePlan은_세션_삭제_후_플랜_헤더를_삭제한다() {
        when(planMapper.findPlanForAuth(10L)).thenReturn(ownedPlan(10L, 100L));

        planService.deletePlan(100L, 10L);

        verify(planMapper).deletePlanSessions(10L);
        verify(planMapper).deletePlan(10L);
    }

    private void verifyNoInteractions_delete() {
        verify(planMapper, never()).deletePlanSessions(any());
        verify(planMapper, never()).deletePlan(any());
    }

    private Plan ownedPlan(Long planNo, Long memberNo) {
        return Plan.builder().planNo(planNo).memberNo(memberNo).build();
    }

    private PlanPlaceRequestDto requestDto(Long placeNo) {
        return new PlanPlaceRequestDto(placeNo, null);
    }

    private void setPlanNo(Plan plan, Long planNo) throws Exception {
        Field field = Plan.class.getDeclaredField("planNo");
        field.setAccessible(true);
        field.set(plan, planNo);
    }
}
