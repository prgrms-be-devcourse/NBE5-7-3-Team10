package kr.co.programmers.collabond.shared.exception;

import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import kr.co.programmers.collabond.shared.exception.custom.*;
import kr.co.programmers.collabond.shared.util.ApiErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class ApiExceptionHandler {

    // CustomException을 전부 handle하는 경우
    @ExceptionHandler({AbstractCustomException.class})
    public <T> ResponseEntity<ApiErrorResponse<T>> handleCustomException(
            AbstractCustomException exception) {

        log.debug("AbstractCustomException: {}", exception.getMessage());
        log.debug("AbstractCustomException Status: {}", exception.getStatus());

        return ApiErrorResponse.error(exception.getMessage(), exception.getStatus());
    }

    @ExceptionHandler({DuplicatedException.class})
    public <T> ResponseEntity<ApiErrorResponse<T>> handleDuplicatedException(
            DuplicatedException exception) {

        log.debug("DuplicatedException: {}", exception.getMessage());

        return ApiErrorResponse.error(exception.getMessage(), exception.getStatus());
    }

    @ExceptionHandler({ExpiredException.class})
    public <T> ResponseEntity<ApiErrorResponse<T>> handleExpiredException(
            ExpiredException exception) {

        log.debug("ExpiredException: {}", exception.getMessage());

        return ApiErrorResponse.error(exception.getMessage(), exception.getStatus());
    }

    @ExceptionHandler({ForbiddenException.class})
    public <T> ResponseEntity<ApiErrorResponse<T>> handleForbiddenException(
            ForbiddenException exception) {

        log.debug("ForbiddenException: {}", exception.getMessage());

        return ApiErrorResponse.error(exception.getMessage(), exception.getStatus());
    }

    @ExceptionHandler({InternalException.class})
    public <T> ResponseEntity<ApiErrorResponse<T>> handleInternalException(
            InternalException exception) {

        log.debug("InternalException: {}", exception.getMessage());

        return ApiErrorResponse.error(exception.getMessage(), exception.getStatus());
    }

    @ExceptionHandler({NotFoundException.class})
    public <T> ResponseEntity<ApiErrorResponse<T>> handleNotFoundException(
            NotFoundException exception) {

        log.debug("NotFoundException: {}", exception.getMessage());

        return ApiErrorResponse.error(exception.getMessage(), exception.getStatus());
    }

    @ExceptionHandler({InvalidException.class})
    public <T> ResponseEntity<ApiErrorResponse<T>> handleInvalidException(
            InvalidException exception) {

        log.debug("InvalidException: {}", exception.getMessage());

        return ApiErrorResponse.error(exception.getMessage(), exception.getStatus());
    }

    @ExceptionHandler({SignatureException.class})
    public <T> ResponseEntity<ApiErrorResponse<T>> handleSignatureException() {
        ForbiddenException exception = new ForbiddenException(ErrorCode.INVALID_SIGNATURE);

        log.debug("SignatureException: {}", exception.getMessage());

        return ApiErrorResponse.error(exception.getMessage(), exception.getStatus());
    }

    @ExceptionHandler({MalformedJwtException.class})
    public <T> ResponseEntity<ApiErrorResponse<T>> handleMalformedJwtException() {
        InvalidException exception = new InvalidException(ErrorCode.INVALID_TOKEN);

        log.debug("MalformedJwtException: {}", exception.getMessage());

        return ApiErrorResponse.error(exception.getMessage(), exception.getStatus());
    }

    @ExceptionHandler({AccessDeniedException.class})
    public <T> ResponseEntity<ApiErrorResponse<T>> handleAccessDeniedException() {
        ForbiddenException exception = new ForbiddenException(ErrorCode.FORBIDDEN_REQUEST);

        log.debug("AccessDeniedException: {}", exception.getMessage());

        return ApiErrorResponse.error(exception.getMessage(), exception.getStatus());
    }
}
