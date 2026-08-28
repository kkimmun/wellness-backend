package com.kh.wellness.ai.model.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CuratorService {
	private final ChatClient chatClient;
	private final static String PERSONA_TEMPLATE = """
			"You are a pro gardener. with 10 years of experience"
			""";
	private final static String TEMPLATE_FOR_SUMMARY = """
					Explain the review summary using the following format rules.
						[Format rules]
						- Do not use Markdown format.
						- Provide feedback within 3 lines.
						- Output only json text.
						- Write the response in Korean.
						***Review Target***
						{text}
										""";
	
	private final static String TEMPLATE_FOR_TITLE_GUIDE = """
					Generate title summary suggestions using the following format rules.
						[Format rules]
						- Do not use Markdown format.
						- Do not expose Line break processing.
						- Provide 3 to 5 title summary suggestions.
						- Output only json text.
						- Write the response in Korean.
						***Review Target***
						{title}
								""";
	private final static String TEMPLATE_FOR_TAG_GUIDE = """
					Generate tag summary suggestions using the following format rules.
						[Format rules]
						- Do not use Markdown format.
						- Do not expose Line break processing.
						- Provide 3 to 5 tag summary as a word.
						- Output only json text.
						- Write the response in Korean.
						***Review Target***
						{tag}
								""";
	
	public String boardSummary(String text) {
			return chatClient.prompt().system(PERSONA_TEMPLATE).user(u -> u.text(TEMPLATE_FOR_SUMMARY).param("text",text)).call().content();
		}
	
	public String boardTitleGuide(String title) {
		return chatClient.prompt().system(PERSONA_TEMPLATE).user(u -> u.text(TEMPLATE_FOR_TITLE_GUIDE).param("title",title)).call().content();
	}
	
	public String boardTagGuide(String tag) {
		return chatClient.prompt().system(PERSONA_TEMPLATE).user(u -> u.text(TEMPLATE_FOR_TAG_GUIDE).param("tag",tag)).call().content();
	}


}
