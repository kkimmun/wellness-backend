package com.kh.wellness.ai.model.service;

import java.util.List;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import com.kh.wellness.ai.model.dto.CourseContent;
import com.kh.wellness.course.model.enums.CourseTag;
import com.kh.wellness.exception.InternalServerException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class OllamaClient {

    private final ChatClient ollamaChatClient;

    public CourseContent generateCourseContent(List<String> placeNames, List<CourseTag> tags) {
        try {
            CourseContent content = ollamaChatClient.prompt()
                    .user(user -> user.text("""
                            다음 정보를 바탕으로 웰니스 관광 코스를 소개해줘.

                            방문 장소: {placeNames}
                            선호 태그: {tags}

                            courseName은 한 줄의 짧은 코스명으로 작성해줘.
                            description은 각 줄을 줄바꿈으로 구분한 2~3줄로 작성해줘.
                            """)
                            .param("placeNames", String.join(", ", placeNames))
                            .param("tags", tags.toString()))
                    .call()
                    .entity(CourseContent.class, spec -> spec.validateSchema());

            if (content == null || content.getCourseName().isBlank() || content.getDescription().isBlank()) {
                throw new IllegalStateException("Ollama가 비어 있는 코스 소개를 반환했습니다.");
            }
            return content;
        } catch (RuntimeException e) {
            log.error("Ollama 코스 소개 생성에 실패했습니다.", e);
            throw new InternalServerException("코스 소개를 생성하지 못했습니다. 잠시 후 다시 시도해주세요.", e);
        }
    }
}
