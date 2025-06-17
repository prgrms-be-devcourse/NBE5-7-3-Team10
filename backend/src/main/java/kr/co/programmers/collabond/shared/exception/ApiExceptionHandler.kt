package kr.co.programmers.collabond.shared.exception

import io.github.oshai.kotlinlogging.KotlinLogging
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.security.SignatureException
import kr.co.programmers.collabond.shared.util.ApiErrorResponse
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

private val logger = KotlinLogging.logger {}

@RestControllerAdvice
class ApiExceptionHandler {

    // CustomException을 전부 handle하는 경우
    @ExceptionHandler(AbstractCustomException::class)
    fun <T> handleCustomException(exception: AbstractCustomException): ResponseEntity<ApiErrorResponse<T>> {
        logger.debug { "AbstractCustomException: ${exception.message}" }
        logger.debug { "AbstractCustomException Status: ${exception.status}" }

        return ApiErrorResponse.error(exception.message, exception.status)
    }

    @ExceptionHandler(DuplicatedException::class)
    fun <T> handleDuplicatedException(exception: DuplicatedException): ResponseEntity<ApiErrorResponse<T>> {
        logger.debug { "DuplicatedException: ${exception.message}" }

        return ApiErrorResponse.error(exception.message, exception.status)
    }

    @ExceptionHandler(ExpiredException::class)
    fun <T> handleExpiredException(exception: ExpiredException): ResponseEntity<ApiErrorResponse<T>> {
        logger.debug { "ExpiredException: ${exception.message}" }

        return ApiErrorResponse.error(exception.message, exception.status)
    }

    @ExceptionHandler(ForbiddenException::class)
    fun <T> handleForbiddenException(exception: ForbiddenException): ResponseEntity<ApiErrorResponse<T>> {
        logger.debug { "ForbiddenException: ${exception.message}" }

        return ApiErrorResponse.error(exception.message, exception.status)
    }

    @ExceptionHandler(InternalException::class)
    fun <T> handleInternalException(exception: InternalException): ResponseEntity<ApiErrorResponse<T>> {
        logger.debug { "InternalException: ${exception.message}" }

        return ApiErrorResponse.error(exception.message, exception.status)
    }

    @ExceptionHandler(NotFoundException::class)
    fun <T> handleNotFoundException(exception: NotFoundException): ResponseEntity<ApiErrorResponse<T>> {
        logger.debug { "NotFoundException: ${exception.message}" }

        return ApiErrorResponse.error(exception.message, exception.status)
    }

    @ExceptionHandler(InvalidException::class)
    fun <T> handleInvalidException(exception: InvalidException): ResponseEntity<ApiErrorResponse<T>> {
        logger.debug { "InvalidException: ${exception.message}" }

        return ApiErrorResponse.error(exception.message, exception.status)
    }

    @ExceptionHandler(SignatureException::class)
    fun <T> handleSignatureException(): ResponseEntity<ApiErrorResponse<T>> {
        val exception = ForbiddenException(ErrorCode.INVALID_SIGNATURE)

        logger.debug { "SignatureException: ${exception.message}" }

        return ApiErrorResponse.error(exception.message, exception.status)
    }

    @ExceptionHandler(MalformedJwtException::class)
    fun <T> handleMalformedJwtException(): ResponseEntity<ApiErrorResponse<T>> {
        val exception = InvalidException(ErrorCode.INVALID_TOKEN)

        logger.debug { "MalformedJwtException: ${exception.message}" }

        return ApiErrorResponse.error(exception.message, exception.status)
    }

    @ExceptionHandler(AccessDeniedException::class)
    fun <T> handleAccessDeniedException(): ResponseEntity<ApiErrorResponse<T>> {
        val exception = ForbiddenException(ErrorCode.FORBIDDEN_REQUEST)

        logger.debug { "AccessDeniedException: ${exception.message}" }

        return ApiErrorResponse.error(exception.message, exception.status)
    }
}