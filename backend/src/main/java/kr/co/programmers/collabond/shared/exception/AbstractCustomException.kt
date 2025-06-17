package kr.co.programmers.collabond.shared.exception

import org.springframework.http.HttpStatus

abstract class AbstractCustomException : RuntimeException {

    val status: HttpStatus

    // message를 non-null로 보장하기 위한 프로퍼티
    override val message: String
        get() = super.message ?: "알 수 없는 오류"

    constructor(errorCode: ErrorCode) : super(errorCode.message) {
        this.status = errorCode.getHttpStatus()
    }

    constructor(errorCode: ErrorCode, message: String) : super(message) {
        this.status = errorCode.getHttpStatus()
    }

    constructor(errorCode: ErrorCode, cause: Throwable) : super(errorCode.message, cause) {
        this.status = errorCode.getHttpStatus()
    }

    // 이 예외 상위에 아무런 trace도 가지지 않도록 this를 호출
    override fun fillInStackTrace(): Throwable = this
}