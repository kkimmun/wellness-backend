package com.kh.wellness.auth.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import com.kh.wellness.auth.model.dto.AccessTokenDto;
import com.kh.wellness.auth.model.dto.TokenResponse;
import com.kh.wellness.auth.model.service.AuthService;
import com.kh.wellness.common.api.ApiResponse;
import com.kh.wellness.token.model.service.TokenService;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private TokenService tokenService;

    private AuthController authController;

    @BeforeEach
    void setUp() {
        authController = new AuthController(authService, tokenService);
    }

    @Test
    void refresh_응답본문에는_액세스토큰만_포함하고_리프레시토큰은_쿠키로_전달한다() {
        when(authService.refresh("old-refresh-token"))
                .thenReturn(new TokenResponse("new-access-token", "new-refresh-token"));

        ResponseEntity<ApiResponse<AccessTokenDto>> response =
                authController.refresh("old-refresh-token");

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData().getAccessToken())
                .isEqualTo("new-access-token");
        assertThat(response.getHeaders().getFirst(HttpHeaders.SET_COOKIE))
                .contains("refreshToken=new-refresh-token")
                .contains("HttpOnly");
    }
}
