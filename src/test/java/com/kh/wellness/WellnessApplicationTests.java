package com.kh.wellness;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"DB_HOST=localhost",
		"DB_PORT=1521",
		"DB_SID=XE",
		"DB_USERNAME=test",
		"DB_PASSWORD=test",
		"MAIL_USERNAME=test@example.com",
		"MAIL_PASSWORD=test-password",
		"AWS_ACCESS_KEY=test-access-key",
		"AWS_SECRET_KEY=test-secret-key",
		"AWS_REGION=ap-northeast-2",
		"S3_BUCKET=test-bucket",
		"JWT_SECRET=MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE=",
		"KAKAO_REST_API_KEY=test-key",
		"spring.config.import=classpath:templates/mail/mail-template.yml",
		"mybatis.configuration.jdbc-type-for-null=VARCHAR",
		"mybatis.configuration.map-underscore-to-camel-case=true",
		"mybatis.configuration.log-impl=org.apache.ibatis.logging.stdout.StdOutImpl",
		"mybatis.mapper-locations=classpath:mapper/**/*.xml",
		"mybatis.type-aliases-package=com.kh.wellness.member.model.vo,com.kh.wellness.token.model.vo,com.kh.wellness.member.model.dto,com.kh.wellness.admin.place.model.dto,com.kh.wellness.admin.course.model.dto,com.kh.wellness.course.model.dto,com.kh.wellness.route.model.vo"
})
class WellnessApplicationTests {

	@Test
	void contextLoads() {
	}

}
