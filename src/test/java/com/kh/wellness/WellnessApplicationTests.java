	package com.kh.wellness;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		// application.yml 은 .gitignore 대상이라 CI 체크아웃에는 존재하지 않는다.
		// 이 테스트는 실제 DB/외부 서비스 없이 스프링 컨텍스트가 뜨는지만 확인하므로,
		// application.yml 이 정의하는 실제 프로퍼티 키를 더미 값으로 직접 지정한다.
		"spring.datasource.url=jdbc:oracle:thin:@localhost:1521:XE",
		"spring.datasource.driver-class-name=oracle.jdbc.driver.OracleDriver",
		"spring.datasource.username=test",
		"spring.datasource.password=test",
		"spring.datasource.hikari.initialization-fail-timeout=-1",
		"spring.mail.host=smtp.gmail.com",
		"spring.mail.port=587",
		"spring.mail.username=test@example.com",
		"spring.mail.password=test-password",
		"spring.mail.properties.mail.smtp.auth=true",
		"spring.mail.properties.mail.smtp.starttls.enable=true",
		"cloud.aws.credentials.access-key=test-access-key",
		"cloud.aws.credentials.secret-key=test-secret-key",
		"cloud.aws.region.static=ap-northeast-2",
		"cloud.aws.s3.bucket=test-bucket",
		"jwt.secret=MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE=",
		"kakao.rest-api-key=test-key",
		"spring.config.import=classpath:templates/mail/mail-template.yml",
		"mybatis.configuration.jdbc-type-for-null=VARCHAR",
		"mybatis.configuration.map-underscore-to-camel-case=true",
		"mybatis.mapper-locations=classpath:mapper/**/*.xml",
		"mybatis.type-aliases-package=com.kh.wellness.member.model.vo,com.kh.wellness.member.model.dto,com.kh.wellness.token.model.vo,com.kh.wellness.admin.place.model.dto,com.kh.wellness.admin.course.model.dto,com.kh.wellness.course.model.dto,com.kh.wellness.course.model.enums,com.kh.wellness.route.model.vo,com.kh.wellness.place.model.vo,com.kh.wellness.plan.model.dto,com.kh.wellness.review.model.vo,com.kh.wellness.review.model.dto,com.kh.wellness.sensor.model.dto"
})
class WellnessApplicationTests {

	@Test
	void contextLoads() {
	}

}
