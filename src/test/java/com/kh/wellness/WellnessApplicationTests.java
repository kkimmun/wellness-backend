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
		"KAKAO_REST_API_KEY=test-key"
})
class WellnessApplicationTests {

	@Test
	void contextLoads() {
	}

}
