package kr.co.programmers.collabond.shared.util

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

data class ApiErrorResponse<T>(
    @field:NotBlank(message = "메시지는 비어있을 수 없습니다")
    val message: String,

    @field:Positive(message = "상태 코드는 양수여야 합니다")
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
    @field:NotBlank(message = "메시지는 비어있을 수 없습니다")
    val message: String,

    @field:Positive(message = "상태 코드는 양수여야 합니다")
    val status: Int,

    @field:NotNull(message = "데이터는 null일 수 없습니다")
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