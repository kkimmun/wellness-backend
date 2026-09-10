package com.kh.wellness.member.model.service;

import static org.assertj.core.api.Assertions.*;
import java.util.Map;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;

class MemberProfileMapperTest {
    @Test void mapperParsesWithoutExtraAliasesAndUsesSeparatedTables() throws Exception {
        Configuration configuration = new Configuration();
        String resource = "mapper/member/MemberProfileMapper.xml";
        try (var stream = getClass().getClassLoader().getResourceAsStream(resource)) {
            assertThat(stream).isNotNull();
            new XMLMapperBuilder(stream, configuration, resource, configuration.getSqlFragments()).parse();
        }
        String ns = "com.kh.wellness.member.model.dao.MemberProfileMapper.";
        String name = configuration.getMappedStatement(ns + "updateName").getBoundSql(Map.of("memberNo", 7, "memberName", "닉네임")).getSql();
        assertThat(name).contains("UPDATE WELLNESS_MEMBER", "DEL_YN = 'N'").doesNotContain("MEMBER_PWD", "EMAIL");
        String pwd = configuration.getMappedStatement(ns + "updatePassword").getBoundSql(Map.of("memberNo", 7, "oldHash", "old", "newHash", "new")).getSql();
        assertThat(pwd).contains("UPDATE WELLNESS_NORMAL_MEMBER", "AND MEMBER_PWD = ?").doesNotContain("SET MEMBER_NAME");
        String photo = configuration.getMappedStatement(ns + "updatePhoto").getBoundSql(Map.of("memberNo", 7)).getSql();
        assertThat(photo).contains("UPDATE WELLNESS_MEMBER", "SAVE_NAME", "IMG_PATH").doesNotContain("MEMBER_IMG");
    }
}
