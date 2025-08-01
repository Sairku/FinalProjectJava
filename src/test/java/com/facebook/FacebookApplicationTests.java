package com.facebook;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
		"app.frontend.url=http://localhost:3000",
		"spring.mail.username=your_email@gmail.com",
		"spring.mail.password=your_email_password",
		"cloudinary.cloud-name=your_cloud_name",
		"cloudinary.api-key=your_api_key",
		"cloudinary.api-secret=your_api_secret"
})
class FacebookApplicationTests {

	@Test
	void contextLoads() {
	}

}
