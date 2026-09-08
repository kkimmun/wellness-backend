package com.kh.wellness.configuration;

import static org.assertj.core.api.Assertions.assertThat;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

class MapperTypeAliasesTest {
    @Test
    void allMappersLoadWithoutAliasCollisions() throws Exception {
        Configuration configuration = new Configuration();
        String[] packages = {
            "com.kh.wellness.member.model.vo",
            "com.kh.wellness.member.model.dto",
            "com.kh.wellness.token.model.vo",
            "com.kh.wellness.admin.place.model.dto",
            "com.kh.wellness.admin.course.model.dto",
            "com.kh.wellness.course.model.dto",
            "com.kh.wellness.course.model.enums",
            "com.kh.wellness.route.model.vo",
            "com.kh.wellness.place.model.vo",
            "com.kh.wellness.plan.model.dto",
            "com.kh.wellness.admin.place.model.vo",
            "com.kh.wellness.place.model.dto"
        };
        for (String packageName : packages) {
            configuration.getTypeAliasRegistry().registerAliases(packageName);
        }
        var resolver = new PathMatchingResourcePatternResolver();
        var resources = resolver.getResources("classpath*:mapper/**/*.xml");
        assertThat(resources).isNotEmpty();
        for (var resource : resources) {
            try (var stream = resource.getInputStream()) {
                new XMLMapperBuilder(stream, configuration, resource.toString(),
                        configuration.getSqlFragments()).parse();
            }
        }
        assertThat(configuration.getTypeAliasRegistry().resolveAlias("Place"))
                .isEqualTo(com.kh.wellness.route.model.vo.Place.class);
        assertThat(configuration.getTypeAliasRegistry().resolveAlias("AdminPlace"))
                .isEqualTo(com.kh.wellness.admin.place.model.vo.Place.class);
        assertThat(configuration.getMappedStatement("com.kh.wellness.place.model.dao.PlaceMapper.selectPlaces")
                .getResultMaps().getFirst().getType())
                .isEqualTo(com.kh.wellness.place.model.dto.PlaceResponse.class);
    }
}
