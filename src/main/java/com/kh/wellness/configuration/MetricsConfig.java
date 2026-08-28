package com.kh.wellness.configuration;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MetricsConfig {
	/*
	 * 해당 부분은 기존 레거시 코드를 가져와서 메트리 수집을 하는 곳 만약 모니터링 툴을 사용할 경우 해당 코드 컨벤션에 맞게 개발 할 것.
	@Bean
	public Counter viewBoardCounter(MeterRegistry registry) {
		return Counter.builder("board_view_total").tag("type", "board")
                .description("게시글 조회 횟수")
                .register(registry);
	}
		
	@Bean
	public Counter viewNoticeCounter(MeterRegistry registry) {
		return Counter.builder("notice_view_total").tag("type", "notice")
                .description("게시글 조회 횟수")
                .register(registry);
	}
	
	@Bean	
	public Gauge viewCounterCurrent(MeterRegistry registry) {
		return Gauge.builder("board_count_current", boardService, service -> service.getTotalElements())
			     .description("현재 게시글 수")
			     .register(registry);
	}
		
	@Bean
	public TimedAspect timedAspect(MeterRegistry registry) {
	    return new TimedAspect(registry);
	} */

}
