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

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.kh.wellness.exception.BadRequestException;
import com.kh.wellness.plan.model.dao.PlanMapper;
import com.kh.wellness.plan.model.dto.PlanPlaceRequestDto;
import com.kh.wellness.plan.model.vo.Plan;

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
    void savePlan은_요청한_장소들을_입력_순서대로_저장하고_순번을_1부터_부여한다() {
        when(planMapper.savePlan(any(Plan.class))).thenReturn(1);

        planService.savePlan(100L, List.of(
                requestDto(11L),
                requestDto(22L),
                requestDto(33L)));

        ArgumentCaptor<Plan> captor = ArgumentCaptor.forClass(Plan.class);
        verify(planMapper, times(3)).savePlan(captor.capture());
        assertThat(captor.getAllValues())
                .extracting(Plan::getMemberNo, Plan::getPlaceNo, Plan::getPlaceOrder)
                .containsExactly(
                        tuple(100L, 11L, 1),
                        tuple(100L, 22L, 2),
                        tuple(100L, 33L, 3));
    }

    @Test
    void savePlan은_요청_DTO의_placeOrder값을_무시하고_리스트_인덱스로_순번을_매긴다() {
        when(planMapper.savePlan(any(Plan.class))).thenReturn(1);
        PlanPlaceRequestDto reversedOrder = new PlanPlaceRequestDto(55L, 99);

        planService.savePlan(1L, List.of(reversedOrder));

        ArgumentCaptor<Plan> captor = ArgumentCaptor.forClass(Plan.class);
        verify(planMapper).savePlan(captor.capture());
        assertThat(captor.getValue().getPlaceOrder()).isEqualTo(1);
    }

    @Test
    void savePlan은_Mapper가_0건_반영을_반환하면_BadRequestException을_던진다() {
        when(planMapper.savePlan(any(Plan.class))).thenReturn(0);

        assertThatThrownBy(() -> planService.savePlan(1L, List.of(requestDto(11L))))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("저장에 실패하였습니다.");
    }

    @Test
    void savePlan은_첫_장소_저장에_실패하면_이후_장소를_저장하지_않는다() {
        when(planMapper.savePlan(any(Plan.class))).thenReturn(0);

        assertThatThrownBy(() -> planService.savePlan(1L, List.of(
                requestDto(11L),
                requestDto(22L))))
                .isInstanceOf(BadRequestException.class);

        verify(planMapper, times(1)).savePlan(any(Plan.class));
    }

    @Test
    void savePlan은_빈_리스트를_받으면_Mapper를_호출하지_않고_정상_종료한다() {
        planService.savePlan(1L, List.of());

        verifyNoInteractions(planMapper);
    }

    @Test
    void editPlan은_기존_계획을_삭제하지_않고_요청한_장소를_저장한다() {
        when(planMapper.savePlan(any(Plan.class))).thenReturn(1);

        planService.editPlan(100L, List.of(requestDto(11L), requestDto(22L)));

        verify(planMapper, never()).deletePlan(any());
        ArgumentCaptor<Plan> captor = ArgumentCaptor.forClass(Plan.class);
        verify(planMapper, times(2)).savePlan(captor.capture());
        assertThat(captor.getAllValues())
                .extracting(Plan::getMemberNo, Plan::getPlaceNo, Plan::getPlaceOrder)
                .containsExactly(tuple(100L, 11L, 1), tuple(100L, 22L, 2));
    }

    @Test
    void editPlan은_저장_단계에서_실패하면_BadRequestException을_전파한다() {
        when(planMapper.savePlan(any(Plan.class))).thenReturn(0);

        assertThatThrownBy(() -> planService.editPlan(100L, List.of(requestDto(11L))))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("저장에 실패하였습니다.");

        verify(planMapper, never()).deletePlan(any());
        verify(planMapper).savePlan(any(Plan.class));
    }

    @Test
    void editPlan은_빈_리스트를_받으면_DB를_변경하지_않는다() {
        planService.editPlan(100L, List.of());

        verifyNoInteractions(planMapper);
    }

    private PlanPlaceRequestDto requestDto(Long placeNo) {
        return new PlanPlaceRequestDto(placeNo, null);
    }
}
