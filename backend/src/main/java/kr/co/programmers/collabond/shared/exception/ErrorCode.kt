package kr.co.programmers.collabond.shared.exception

import org.springframework.http.HttpStatus

enum class ErrorCode(
    val status: Int,
    val message: String
) {
    INVALID_REQUEST(400, "요청이 올바르지 않습니다"),
    INVALID_TOKEN(400, "유효하지 않은 토큰입니다"),
    INVALID_PROVIDER(400, "유효하지 않은 OAuth2.0 제공자입니다"),
    INVALID_SIGNUP_REQUEST(400, "이미 회원가입 했습니다"),
    REQUEST_TO_SAME_ROLE(400, "같은 직종의 사용자에게 지원서를 제출할 수 없습니다"),
    DUPLICATED_DATA(400, "중복된 데이터입니다"),
    DUPLICATED_CHARACTER_NAME(400, "중복되는 캐릭터 이름입니다"),
    DUPLICATED_USER_EMAIL(400, "이미 가입된 이메일입니다"),
    DUPLICATED_TAG(400, "이름과 타입이 같은 태그가 이미 존재합니다"),
    REMOVED_RECRUIT_POST(400, "삭제된 모집글은 수정할 수 없습니다"),
    CREATE_NO_MORE(400, "유효하지 않은 토큰입니다"),
    OVER_MAX_TAG(400, "태그는 최대 5개까지 선택할 수 있습니다"),
    NOT_MATCH_TYPE_OF_TAG(400, "프로필 타입과 일치하지 않는 태그가 포함되어 있습니다"),

    EXPIRED_DATA(401, "만료된 데이터입니다"),
    EXPIRED_TOKEN(401, "만료된 토큰입니다"),

    FORBIDDEN_REQUEST(403, "요청을 수행할 권한이 없습니다"),
    INVALID_SIGNATURE(403, "유효하지 않은 시그니처입니다"),

    NOT_FOUND(404, "요청에 대한 데이터를 찾을 수 없습니다"),
    RECRUIT_NOT_FOUND(404, "모집 공고글을 찾을 수 없습니다"),
    PROFILE_NOT_FOUND(404, "프로필을 찾을 수 없습니다"),
    USER_NOT_FOUND(404, "회원 정보를 찾을 수 없습니다"),
    APPLY_NOT_FOUND(404, "제출한 지원글을 찾을 수 없습니다"),
    TOKEN_NOT_FOUND(404, "토큰을 찾을 수 없습니다"),
    TAG_NOT_FOUND(404, "태그를 찾을 수 없습니다"),
    INCLUDE_TAG_NOT_FOUND(404, "존재하지 않는 태그가 포함되어 있습니다"),

    EXPIRED_REFRESH_TOKEN(410, "만료된 리프레시 토큰입니다"),

    INTERNAL_SERVER_ERROR(500, "내부 서버 오류"),
    SAVE_IMAGE_ERROR(500, "이미지 저장 실패");

    // status값으로 HttpStatus를 반환하는 메서드
    fun getHttpStatus(): HttpStatus = HttpStatus.valueOf(status)
}