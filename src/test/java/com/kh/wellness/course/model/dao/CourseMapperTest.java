package com.kh.wellness.course.model.dao;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;

import com.kh.wellness.course.model.dto.CourseListRow;
import com.kh.wellness.course.model.dto.CourseResponse;
import com.kh.wellness.course.model.dto.PlaceDto;
import com.kh.wellness.course.model.dto.WaypointDto;
import com.kh.wellness.course.model.enums.CourseTag;

class CourseMapperTest {
    @Test
    void restaurantStatementLoadsWithExistingAliasesAndGroupsTagsByPlace() throws Exception {
        Configuration configuration = new Configuration();
        for (Class<?> type : new Class<?>[] {CourseListRow.class, CourseResponse.class, PlaceDto.class,
                WaypointDto.class, CourseTag.class}) {
            configuration.getTypeAliasRegistry().registerAlias(type);
        }
        String resource = "mapper/course/CourseMapper.xml";
        try (var stream = getClass().getClassLoader().getResourceAsStream(resource)) {
            assertThat(stream).isNotNull();
            new XMLMapperBuilder(stream, configuration, resource, configuration.getSqlFragments()).parse();
        }
        var statement = configuration.getMappedStatement(CourseMapper.class.getName() + ".selectRestaurants");
        var placeMap = statement.getResultMaps().getFirst();
        assertThat(placeMap.getType()).isEqualTo(PlaceDto.class);
        assertThat(placeMap.getIdResultMappings()).extracting(mapping -> mapping.getProperty())
                .containsExactly("placeNo");
        assertThat(placeMap.getResultMappings()).anySatisfy(mapping -> {
            assertThat(mapping.getProperty()).isEqualTo("tags");
            assertThat(mapping.getNotNullColumns()).contains("TAG_CONTENT");
        });
        String sql = statement.getBoundSql(null).getSql().replaceAll("\\s+", " ");
        assertThat(sql).contains("T.TYPE_NO = 6", "P.DEL_YN = 'N'", "LEFT JOIN PLACE_TAG", "LEFT JOIN TAG");
    }
}
