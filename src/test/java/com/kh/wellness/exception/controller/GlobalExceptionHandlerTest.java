package com.kh.wellness.exception.controller;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.kh.wellness.common.api.ApiResponse;

import jakarta.mail.MessagingException;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void messagingException_일관된_ApiResponse로_변환한다() {
        ResponseEntity<ApiResponse<Void>> response =
                handler.handleMessagingException(new MessagingException("mail server error"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(500);
        assertThat(response.getBody().getMessage()).isEqualTo("인증 메일 발송에 실패했습니다.");
        assertThat(response.getBody().getData()).isNull();
    }
}
