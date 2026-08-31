package com.kh.wellness.configuration;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {
	
	@Bean
	ChatClient chatClient(ChatClient.Builder builder) {
		return builder.defaultSystem("비속어가 노출되지 않도록 할 것").build();
	}
}
