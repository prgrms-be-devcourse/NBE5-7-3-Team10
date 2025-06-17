package kr.co.programmers.collabond.core.auth.oauth2

interface OAuth2ResponseDto {
    // 제공자 (Ex. naver, google, ...)
    val provider: String

    // 제공자에서 발급해주는 아이디(번호)
    val providerId: String

    // 이메일
    val email: String

    // 사용자 실명 (설정한 이름)
    val name: String
}

class NaverResponseDto(attributes: Map<String, Any>) : OAuth2ResponseDto {
    private val response: Map<String, Any> = attributes["response"] as Map<String, Any>

    override val provider: String = "naver"
    override val providerId: String = response["id"].toString()
    override val email: String = response["email"].toString()
    override val name: String = response["name"].toString()
}

class GoogleResponseDto(private val attributes: Map<String, Any>) : OAuth2ResponseDto {
    override val provider: String = "google"
    override val providerId: String = attributes["sub"].toString()
    override val email: String = attributes["email"].toString()
    override val name: String = "임시 닉네임"
}

class KakaoResponseDto(attributes: Map<String, Any>) : OAuth2ResponseDto {
    private val id: String = attributes["id"].toString()
    private val kakaoAccount: Map<String, Any> = attributes["kakao_account"] as Map<String, Any>

    override val provider: String = "kakao"
    override val providerId: String = id
    override val email: String = kakaoAccount["email"].toString()
    override val name: String = "임시 닉네임"
}