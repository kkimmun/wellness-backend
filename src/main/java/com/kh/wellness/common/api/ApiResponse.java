package com.kh.wellness.common.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    private int code;
    private String message;
    private T data;

    // 200 성공 응답
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(200, message, data);
    }

    // 201 성공 응답
    public static <T> ApiResponse<T> created(String message, T data) {
        return new ApiResponse<>(201, message, data);
    }

    // 204 성공 응답
    public static <T> ApiResponse<T> noContent(String message, T data) {
        return new ApiResponse<>(204, message, data);
    }

    // 400 실패 응답
    public static <T> ApiResponse<T> badRequest(String message, T data) {
        return new ApiResponse<>(400, message, data);
    }

    // 401 실패 응답
    public static <T> ApiResponse<T> unAuthorized(String message, T data) {
        return new ApiResponse<>(401, message, data);
    }

    // 403 실패 응답
    public static <T> ApiResponse<T> forbidden(String message, T data) {
        return new ApiResponse<>(403, message, data);
    }

    // 404 실패 응답
    public static <T> ApiResponse<T> notFound(String message, T data) {
        return new ApiResponse<>(404, message, data);
    }

    // 409 실패 응답
    public static <T> ApiResponse<T> conflict(String message, T data) {
        return new ApiResponse<>(409, message, data);
    }

    // 500 실패 응답
    public static <T> ApiResponse<T> internalServerError(String message, T data) {
        return new ApiResponse<>(500, message, data);
    }
}
