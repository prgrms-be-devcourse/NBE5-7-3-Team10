package kr.co.programmers.collabond.shared.util

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

data class ApiErrorResponse<T>(
    val message: String,
    val status: Int
) {
    companion object {
        fun <T> error(message: String, status: HttpStatus): ResponseEntity<ApiErrorResponse<T>> {
            return ResponseEntity.status(status)
                .body(ApiErrorResponse(message, status.value()))
        }
    }
}

data class ApiSuccessResponse<T>(
    val message: String,
    val status: Int,
    val data: T
) {
    companion object {
        fun <T> ok(data: T, message: String): ResponseEntity<ApiSuccessResponse<T>> {
            return ResponseEntity.status(HttpStatus.OK)
                .body(ApiSuccessResponse(message, HttpStatus.OK.value(), data))
        }

        fun <T> created(data: T, message: String): ResponseEntity<ApiSuccessResponse<T>> {
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiSuccessResponse(message, HttpStatus.CREATED.value(), data))
        }
    }
}