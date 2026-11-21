package com.example.reposearcher.exception;

import com.example.reposearcher.dto.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage()).collect(Collectors.joining("; "));
        return build(HttpStatus.BAD_REQUEST, msg, null);
    }

    @ExceptionHandler({ConstraintViolationException.class, InvalidRequestException.class,
            HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ErrorResponse> badRequest(Exception e) {
        return build(HttpStatus.BAD_REQUEST, e instanceof HttpMessageNotReadableException
                ? "Malformed or missing JSON request body" : e.getMessage(), null);
    }

    @ExceptionHandler(RateLimitException.class)
    public ResponseEntity<ErrorResponse> rateLimit(RateLimitException e) {
        return build(HttpStatus.TOO_MANY_REQUESTS, e.getMessage(), e.getRetryAfter());
    }

    @ExceptionHandler(UpstreamException.class)
    public ResponseEntity<ErrorResponse> upstream(UpstreamException e) {
        return build(HttpStatus.BAD_GATEWAY, e.getMessage(), null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> other(Exception e) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error", null);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus s, String msg, String retryAfter) {
        ResponseEntity.BodyBuilder b = ResponseEntity.status(s);
        if (retryAfter != null) b.header(HttpHeaders.RETRY_AFTER, retryAfter);
        return b.body(new ErrorResponse(s.value(), s.getReasonPhrase(), msg));
    }
}
