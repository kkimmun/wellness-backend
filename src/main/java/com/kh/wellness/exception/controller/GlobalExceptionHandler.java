package com.kh.wellness.exception.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.kh.wellness.common.api.ApiResponse;
import com.kh.wellness.exception.BadRequestException;
import com.kh.wellness.exception.ConflictException;
import com.kh.wellness.exception.FileException;
import com.kh.wellness.exception.ForbiddenException;
import com.kh.wellness.exception.InternalServerException;
import com.kh.wellness.exception.NotFoundException;
import com.kh.wellness.exception.UnauthorizedException;
import com.kh.wellness.exception.ValidationException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 400 Bad Request
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(
            BadRequestException e) {

        return ResponseEntity
                .badRequest()
                .body(ApiResponse.badRequest(e.getMessage(), null));
    }

    // 400 Validation
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(
            ValidationException e) {

        return ResponseEntity
                .badRequest()
                .body(ApiResponse.badRequest(e.getMessage(), null));
    }

    // 401 Unauthorized
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnauthorized(
            UnauthorizedException e) {

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.unAuthorized(e.getMessage(), null));
    }

    // 403 Forbidden
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ApiResponse<Void>> handleForbidden(
            ForbiddenException e) {

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.forbidden(e.getMessage(), null));
    }

    // 404 Not Found
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(
            NotFoundException e) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.notFound(e.getMessage(), null));
    }

    // 409 Conflict
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiResponse<Void>> handleConflict(
            ConflictException e) {

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.conflict(e.getMessage(), null));
    }

    // 500 File Error
    @ExceptionHandler(FileException.class)
    public ResponseEntity<ApiResponse<Void>> handleFile(
            FileException e) {

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.internalServerError(e.getMessage(), null));
    }

    // 500 Internal Server Error
    @ExceptionHandler(InternalServerException.class)
    public ResponseEntity<ApiResponse<Void>> handleInternalServer(
            InternalServerException e) {

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.internalServerError(e.getMessage(), null));
    }

    // @Valid RequestBody 검증 실패
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<List<String>>> handleMethodArgumentNotValid(
            MethodArgumentNotValidException e) {
        List<String> messages = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> fieldError.getDefaultMessage())
                .distinct()
                .toList();
        String message = messages.isEmpty()
                ? "입력값을 확인해주세요."
                : messages.getFirst();

        return ResponseEntity
                .badRequest()
                .body(ApiResponse.badRequest(message, messages));
    }

    // JSON 문법 오류 또는 요청 본문 타입 불일치
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadable(
            HttpMessageNotReadableException e) {
        return ResponseEntity
                .badRequest()
                .body(ApiResponse.badRequest("요청 본문 형식을 확인해주세요.", null));
    }

    // RequestParam 또는 PathVariable 타입 불일치
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException e) {
        return ResponseEntity
                .badRequest()
                .body(ApiResponse.badRequest("요청값 형식을 확인해주세요.", null));
    }

    // 예상하지 못한 Exception 최종 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(
            Exception e) {

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.internalServerError(
                        "서버 내부 오류가 발생했습니다.",
                        null
                ));
    }

    //@Valid 검증 실패 시 예외 처리
    @ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ApiResponse> handleConstraintViolation(ConstraintViolationException e) {
	    List<String> messages = e.getConstraintViolations()
	            .stream()
	            .map(ConstraintViolation::getMessage)
	            .toList();

	    return ResponseEntity
	            .badRequest()
	            .body(ApiResponse.badRequest("올바른 형식이 아닙니다.", messages));
	}


}
