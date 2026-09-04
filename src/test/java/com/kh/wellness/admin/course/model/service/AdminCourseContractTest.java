package com.kh.wellness.admin.course.model.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;
import jakarta.validation.Validation;
import com.kh.wellness.admin.course.model.dto.AdminCourseRequest;
import com.kh.wellness.admin.course.model.dto.AdminCourseDetailResponse;
import com.kh.wellness.course.model.dto.CourseResponse;
import tools.jackson.databind.ObjectMapper;

class AdminCourseContractTest {
    @Test
    void requestWithoutEstimatedTimeDeserializesAndValidates() {
        ObjectMapper mapper = new ObjectMapper();
        AdminCourseRequest request = mapper.readValue("""
                {"courseName":"김포 힐링 코스","description":"코스 설명",
                 "startPlaceNo":1,"waypointPlaceNos":[2,3,4],"endPlaceNo":5}
                """, AdminCourseRequest.class);
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            assertThat(factory.getValidator().validate(request)).isEmpty();
        }
        assertThat(request.getWaypointPlaceNos()).containsExactly(2L, 3L, 4L);
        assertThat(mapper.writeValueAsString(request)).doesNotContain("estimatedTime");
    }

    @Test
    void requiredEndpointsAndWaypointLimitStillValidate() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var validator = factory.getValidator();
            var missingStart = new AdminCourseRequest("코스", "설명", null, List.of(), 5L);
            assertThat(validator.validate(missingStart)).anyMatch(v -> v.getPropertyPath().toString().equals("startPlaceNo"));
            var tooManyStops = new AdminCourseRequest("코스", "설명", 1L, List.of(2L, 3L, 4L, 5L), 6L);
            assertThat(validator.validate(tooManyStops)).anyMatch(v -> v.getPropertyPath().toString().equals("waypointPlaceNos"));
        }
    }

    @Test
    void detailResponsesAndSqlNoLongerDependOnStoredTime() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        assertThat(mapper.writeValueAsString(new AdminCourseDetailResponse())).doesNotContain("estimatedTime");
        assertThat(mapper.writeValueAsString(new CourseResponse())).doesNotContain("estimatedTime");
        for (String path : List.of("/mapper/admin/course/AdminCourseMapper.xml", "/mapper/course/CourseMapper.xml")) {
            try (var stream = getClass().getResourceAsStream(path)) {
                assertThat(stream).isNotNull();
                String sql = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
                assertThat(sql).doesNotContain("ESTIMATED_TIME", "#{estimatedTime}");
            }
        }
    }
}
