package com.kh.wellness.plan.model.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
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
    void createPlan은_헤더와_장소를_한번에_순서대로_저장한다() throws Exception {
        when(planMapper.countAvailablePlaces(List.of(11L, 22L))).thenReturn(2);
        when(planMapper.insertPlan(any(Plan.class))).thenAnswer(invocation -> {
            Plan plan = invocation.getArgument(0);
            setPlanNo(plan, 10L);
            return 1;
        });
        when(planMapper.insertPlanSession(any(PlanSession.class))).thenReturn(1);

        Long planNo = planService.createPlan(100L, request("여행", 11L, 22L));

        assertThat(planNo).isEqualTo(10L);
        ArgumentCaptor<PlanSession> captor = ArgumentCaptor.forClass(PlanSession.class);
        verify(planMapper, times(2)).insertPlanSession(captor.capture());
        assertThat(captor.getAllValues())
                .extracting(PlanSession::getPlanNo, PlanSession::getPlaceNo, PlanSession::getPlaceOrder)
                .containsExactly(tuple(10L, 11L, 1), tuple(10L, 22L, 2));
    }

    @Test
    void createPlan은_같은_장소를_중복해서_저장하지_않는다() {
        assertThatThrownBy(() -> planService.createPlan(100L, request("여행", 11L, 11L)))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("같은 장소를 중복해서 저장할 수 없습니다.");

        verify(planMapper, never()).insertPlan(any());
    }

    @Test
    void createPlan은_존재하지_않거나_삭제된_장소를_저장하지_않는다() {
        when(planMapper.countAvailablePlaces(List.of(11L, 22L))).thenReturn(1);

        assertThatThrownBy(() -> planService.createPlan(100L, request("여행", 11L, 22L)))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("저장할 수 없는 장소가 포함되어 있습니다.");

        verify(planMapper, never()).insertPlan(any());
    }

    @Test
    void createPlan은_빈_장소_목록과_11개_이상_장소를_거부한다() {
        assertThatThrownBy(() -> planService.createPlan(
                100L, new PlanCreateRequestDto("여행", 127.0, 37.0, List.of())))
                .isInstanceOf(BadRequestException.class);

        List<PlanPlaceRequestDto> elevenPlaces = java.util.stream.LongStream.rangeClosed(1, 11)
                .mapToObj(this::requestDto)
                .toList();
        assertThatThrownBy(() -> planService.createPlan(
                100L, new PlanCreateRequestDto("여행", 127.0, 37.0, elevenPlaces)))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void createPlan은_잘못된_이름과_좌표를_거부한다() {
        assertThatThrownBy(() -> planService.createPlan(
                100L, new PlanCreateRequestDto("   ", 127.0, 37.0, List.of(requestDto(11L)))))
                .isInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> planService.createPlan(
                100L, new PlanCreateRequestDto("여행", null, 37.0, List.of(requestDto(11L)))))
                .isInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> planService.createPlan(
                100L, new PlanCreateRequestDto("여행", 181.0, 37.0, List.of(requestDto(11L)))))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void createPlan은_null_장소와_null_장소번호를_거부한다() {
        PlanCreateRequestDto nullPlace = new PlanCreateRequestDto(
                "여행", 127.0, 37.0,
                java.util.Arrays.asList((PlanPlaceRequestDto) null));
        PlanCreateRequestDto nullPlaceNo = new PlanCreateRequestDto(
                "여행", 127.0, 37.0,
                List.of(new PlanPlaceRequestDto(null, null)));

        assertThatThrownBy(() -> planService.createPlan(100L, nullPlace))
                .isInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> planService.createPlan(100L, nullPlaceNo))
                .isInstanceOf(BadRequestException.class);
        verify(planMapper, never()).insertPlan(any());
    }

    @Test
    void createPlan은_Mapper가_0건_반영을_반환하면_BadRequestException을_던진다() {
        when(planMapper.countAvailablePlaces(List.of(11L))).thenReturn(1);
        when(planMapper.insertPlan(any(Plan.class))).thenReturn(0);

        assertThatThrownBy(() -> planService.createPlan(100L, request("여행", 11L)))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void findPlans는_Mapper가_조회한_목록_응답을_반환한다() {
        PlanResponseDto plan = new PlanResponseDto(10L, "김포 여행", 126.6, 37.6, null, 2);
        when(planMapper.findPlansByMember(100L)).thenReturn(List.of(plan));

        assertThat(planService.findPlans(100L)).containsExactly(plan);
    }

    @Test
    void findPlan은_본인_계획의_타입정보와_장소를_반환한다() {
        Plan plan = Plan.builder()
                .planNo(10L)
                .memberNo(100L)
                .planName("김포 여행")
                .xAxis(126.6)
                .yAxis(37.6)
                .build();
        PlanPlaceResponseDto place = new PlanPlaceResponseDto(
                11L, 1, "장소", "설명", "주소", null,
                126.61, 37.61, 1L, 46L, "주요관광지", "김포 TOP 10", null, null);
        when(planMapper.findPlanForAuth(10L)).thenReturn(plan);
        when(planMapper.findPlacesByPlanNo(10L)).thenReturn(List.of(place));

        SavedPlanResponseDto result = planService.findPlan(100L, 10L);

        assertThat(result.getPlanNo()).isEqualTo(10L);
        assertThat(result.getPlaces()).containsExactly(place);
        assertThat(result.getPlaces().getFirst().getType()).isEqualTo("주요관광지");
    }

    @Test
    void updatePlan은_헤더와_장소를_한_트랜잭션_흐름으로_교체한다() {
        when(planMapper.findPlanForAuth(10L)).thenReturn(ownedPlan(10L, 100L));
        when(planMapper.countAvailablePlaces(List.of(22L, 11L))).thenReturn(2);
        when(planMapper.updatePlan(any(Plan.class))).thenReturn(1);
        when(planMapper.insertPlanSession(any(PlanSession.class))).thenReturn(1);

        planService.updatePlan(100L, 10L, request("수정 계획", 22L, 11L));

        InOrder order = inOrder(planMapper);
        order.verify(planMapper).findPlanForAuth(10L);
        order.verify(planMapper).countAvailablePlaces(List.of(22L, 11L));
        order.verify(planMapper).updatePlan(any(Plan.class));
        order.verify(planMapper).deletePlanSessions(10L);
        order.verify(planMapper, times(2)).insertPlanSession(any(PlanSession.class));
    }

    @Test
    void updatePlan은_소유자가_아니면_수정하지_않는다() {
        when(planMapper.findPlanForAuth(10L)).thenReturn(ownedPlan(10L, 999L));

        assertThatThrownBy(() -> planService.updatePlan(100L, 10L, request("수정", 11L)))
                .isInstanceOf(ForbiddenException.class);

        verify(planMapper, never()).updatePlan(any());
        verify(planMapper, never()).deletePlanSessions(any());
    }

    @Test
    void updatePlan은_헤더_수정이_실패하면_기존_장소를_삭제하지_않는다() {
        when(planMapper.findPlanForAuth(10L)).thenReturn(ownedPlan(10L, 100L));
        when(planMapper.countAvailablePlaces(List.of(11L))).thenReturn(1);
        when(planMapper.updatePlan(any(Plan.class))).thenReturn(0);

        assertThatThrownBy(() -> planService.updatePlan(100L, 10L, request("수정", 11L)))
                .isInstanceOf(BadRequestException.class);

        verify(planMapper, never()).deletePlanSessions(any());
    }

    @Test
    void updatePlan은_중복_장소가_있으면_기존_계획을_변경하지_않는다() {
        when(planMapper.findPlanForAuth(10L)).thenReturn(ownedPlan(10L, 100L));

        assertThatThrownBy(() -> planService.updatePlan(
                100L, 10L, request("수정", 11L, 11L)))
                .isInstanceOf(BadRequestException.class);

        verify(planMapper, never()).updatePlan(any());
        verify(planMapper, never()).deletePlanSessions(any());
        verify(planMapper, never()).insertPlanSession(any());
    }

    @Test
    void deletePlan은_본인_소유_플랜이_아니면_삭제하지_않는다() {
        when(planMapper.findPlanForAuth(10L)).thenReturn(ownedPlan(10L, 999L));

        assertThatThrownBy(() -> planService.deletePlan(100L, 10L))
                .isInstanceOf(ForbiddenException.class);

        verify(planMapper, never()).deletePlanSessions(any());
        verify(planMapper, never()).deletePlan(any());
    }

    @Test
    void deletePlan은_세션_삭제_후_플랜_헤더를_삭제한다() {
        when(planMapper.findPlanForAuth(10L)).thenReturn(ownedPlan(10L, 100L));

        planService.deletePlan(100L, 10L);

        verify(planMapper).deletePlanSessions(10L);
        verify(planMapper).deletePlan(10L);
    }

    @Test
    void findPlan은_존재하지_않는_계획이면_NotFoundException을_던진다() {
        when(planMapper.findPlanForAuth(10L)).thenReturn(null);

        assertThatThrownBy(() -> planService.findPlan(100L, 10L))
                .isInstanceOf(NotFoundException.class);
    }

    private PlanCreateRequestDto request(String name, Long... placeNos) {
        return new PlanCreateRequestDto(
                name,
                126.7,
                37.7,
                java.util.Arrays.stream(placeNos).map(this::requestDto).toList());
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
