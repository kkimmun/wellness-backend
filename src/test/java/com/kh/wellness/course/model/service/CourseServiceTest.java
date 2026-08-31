package com.kh.wellness.course.model.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.kh.wellness.common.page.PageResponse;
import com.kh.wellness.course.model.dao.CourseMapper;
import com.kh.wellness.course.model.dto.CourseListResponse;
import com.kh.wellness.course.model.dto.CourseListRow;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock
    private CourseMapper courseMapper;

    private CourseService courseService;

    @BeforeEach
    void setUp() {
        courseService = new CourseService(courseMapper);
    }

    @Test
    void getCoursesMapsStartAndEndPlaces() {
        CourseListRow row = new CourseListRow(
                1L,
                "문수산 마음쉼길",
                10L,
                "김포시청",
                101L,
                "문수산성 사찰",
                210,
                "자연과 휴식을 중심으로 구성한 힐링 순례 코스입니다.");
        when(courseMapper.countActiveCourses()).thenReturn(1L);
        when(courseMapper.selectActiveCourses(0L, 10)).thenReturn(List.of(row));

        PageResponse<CourseListResponse> result = courseService.getCourses(1);

        assertThat(result.getContent()).hasSize(1);
        CourseListResponse course = result.getContent().getFirst();
        assertThat(course.getStartPlace().getPlaceNo()).isEqualTo(10L);
        assertThat(course.getStartPlace().getPlaceName()).isEqualTo("김포시청");
        assertThat(course.getEndPlace().getPlaceNo()).isEqualTo(101L);
        assertThat(course.getEndPlace().getPlaceName()).isEqualTo("문수산성 사찰");
        verify(courseMapper).selectActiveCourses(0L, 10);
    }
}
