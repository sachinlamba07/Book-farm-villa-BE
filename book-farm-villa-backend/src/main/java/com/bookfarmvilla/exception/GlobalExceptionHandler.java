package com.bookfarmvilla.exception;

import com.bookfarmvilla.ai.AiProviderException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Centralized exception handling. Every error response, whatever the cause,
 * comes back in the same JSON shape (see ErrorResponse), which makes the
 * frontend's error handling straightforward.
 *
 * Logging policy: 4xx (client) errors are logged at WARN without a stack trace,
 * 5xx (server) errors are logged at ERROR with the full stack trace.
 *
 * For unexpected 5xx errors the client gets a generic message by default. Set
 * {@code app.error.include-details=true} (env: ERROR_INCLUDE_DETAILS) to return the
 * real exception class + message instead - useful in development, avoid in production.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static final String GENERIC_SERVER_ERROR = "Something went wrong. Please try again later.";

    private final boolean includeDetails;

    public GlobalExceptionHandler(@Value("${app.error.include-details:false}") boolean includeDetails) {
        this.includeDetails = includeDetails;
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex, HttpServletRequest req) {
        return clientError(HttpStatus.NOT_FOUND, ex.getMessage(), ex, req);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(BadRequestException ex, HttpServletRequest req) {
        return clientError(HttpStatus.BAD_REQUEST, ex.getMessage(), ex, req);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicate(DuplicateResourceException ex, HttpServletRequest req) {
        return clientError(HttpStatus.CONFLICT, ex.getMessage(), ex, req);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(UnauthorizedException ex, HttpServletRequest req) {
        return clientError(HttpStatus.UNAUTHORIZED, ex.getMessage(), ex, req);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex, HttpServletRequest req) {
        return clientError(HttpStatus.UNAUTHORIZED, "Invalid email or password", ex, req);
    }

    @ExceptionHandler(AccountLockedException.class)
    public ResponseEntity<ErrorResponse> handleAccountLocked(AccountLockedException ex, HttpServletRequest req) {
        return clientError(HttpStatus.LOCKED, ex.getMessage(), ex, req);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest req) {
        return clientError(HttpStatus.FORBIDDEN, "You do not have permission to perform this action", ex, req);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest req) {
        return clientError(HttpStatus.CONFLICT,
                "This operation violates a data constraint (e.g. a duplicate unique value)", ex, req);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String, String> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fe -> fe.getDefaultMessage() == null ? "Invalid value" : fe.getDefaultMessage(),
                        (a, b) -> a
                ));
        log.warn("{} {} -> 400 Validation failed: {}", req.getMethod(), req.getRequestURI(), fieldErrors);
        ErrorResponse body = ErrorResponse.ofValidation(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Validation failed for one or more fields",
                req.getRequestURI(),
                fieldErrors
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest req) {
        return clientError(HttpStatus.BAD_REQUEST, "Validation failed: " + ex.getMessage(), ex, req);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadableBody(HttpMessageNotReadableException ex, HttpServletRequest req) {
        return clientError(HttpStatus.BAD_REQUEST, "Malformed or unreadable request body", ex, req);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
        String message = "Invalid value '" + ex.getValue() + "' for parameter '" + ex.getName() + "'";
        return clientError(HttpStatus.BAD_REQUEST, message, ex, req);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParam(MissingServletRequestParameterException ex, HttpServletRequest req) {
        return clientError(HttpStatus.BAD_REQUEST, "Missing required parameter '" + ex.getParameterName() + "'", ex, req);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex, HttpServletRequest req) {
        return clientError(HttpStatus.METHOD_NOT_ALLOWED, ex.getMessage(), ex, req);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResource(NoResourceFoundException ex, HttpServletRequest req) {
        return clientError(HttpStatus.NOT_FOUND, "No resource found at " + req.getRequestURI(), ex, req);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest req) {
        return clientError(HttpStatus.BAD_REQUEST, ex.getMessage(), ex, req);
    }

    @ExceptionHandler(AiProviderException.class)
    public ResponseEntity<ErrorResponse> handleAiProvider(AiProviderException ex, HttpServletRequest req) {
        return serverError(HttpStatus.BAD_GATEWAY,
                "The AI service is temporarily unavailable. Please try again shortly.", ex, req);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest req) {
        return serverError(HttpStatus.INTERNAL_SERVER_ERROR, GENERIC_SERVER_ERROR, ex, req);
    }

    /** 4xx: expected client mistakes - WARN, no stack trace. */
    private ResponseEntity<ErrorResponse> clientError(HttpStatus status, String message, Exception ex, HttpServletRequest req) {
        log.warn("{} {} -> {} {}: {} ({})", req.getMethod(), req.getRequestURI(),
                status.value(), status.getReasonPhrase(), message, ex.getClass().getSimpleName());
        return build(status, message, req);
    }

    /** 5xx: server-side failures - ERROR with the full stack trace. */
    private ResponseEntity<ErrorResponse> serverError(HttpStatus status, String message, Exception ex, HttpServletRequest req) {
        log.error("{} {} -> {} {}: {}", req.getMethod(), req.getRequestURI(),
                status.value(), status.getReasonPhrase(), ex.getMessage(), ex);
        String responseMessage = includeDetails
                ? ex.getClass().getName() + ": " + ex.getMessage()
                : message;
        return build(status, responseMessage, req);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message, HttpServletRequest req) {
        ErrorResponse body = ErrorResponse.of(status.value(), status.getReasonPhrase(), message, req.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }
}
