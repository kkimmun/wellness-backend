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
    void editPlaces는_기존_계획을_삭제하지_않고_요청한_장소를_저장한다() {
        when(planMapper.findPlanForAuth(10L)).thenReturn(ownedPlan(10L, 100L));
        when(planMapper.insertPlanSession(any(PlanSession.class))).thenReturn(1);

        planService.editPlaces(100L, 10L, List.of(requestDto(11L), requestDto(22L)));

        verify(planMapper, never()).deletePlanSessions(any());
        ArgumentCaptor<PlanSession> captor = ArgumentCaptor.forClass(PlanSession.class);
        verify(planMapper, times(2)).insertPlanSession(captor.capture());
        assertThat(captor.getAllValues())
                .extracting(PlanSession::getPlanNo, PlanSession::getPlaceNo, PlanSession::getPlaceOrder)
                .containsExactly(tuple(10L, 11L, 1), tuple(10L, 22L, 2));
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
