package com.kh.wellness.configuration;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OllamaConfig {

    @Bean
    public ChatClient ollamaChatClient(ChatClient.Builder builder) {
        return builder
                .defaultSystem("""
                        너는 김포 웰니스 관광 코스의 이름과 소개를 작성하는 작가다.
                        모든 내용은 자연스러운 한국어로 작성하고, 제공되지 않은 장소는 만들지 않는다.
                        코스 이름은 짧고 기억하기 쉽게 작성한다.
                        응답은 요청한 JSON 형식 하나만 출력하고, JSON 밖의 텍스트는 출력하지 않는다.
                        """)
                .build();
    }
}
