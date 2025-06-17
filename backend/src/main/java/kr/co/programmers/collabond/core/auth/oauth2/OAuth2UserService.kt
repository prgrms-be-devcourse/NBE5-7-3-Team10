package kr.co.programmers.collabond.core.auth.oauth2

import jakarta.transaction.Transactional
import kr.co.programmers.collabond.api.user.domain.Role
import kr.co.programmers.collabond.api.user.infrastructure.UserRepository
import kr.co.programmers.collabond.core.auth.oauth2.OAuth2Mapper.toOAuth2UserInfo
import kr.co.programmers.collabond.core.auth.oauth2.OAuth2Mapper.toUser
import kr.co.programmers.collabond.shared.exception.ErrorCode
import kr.co.programmers.collabond.shared.exception.DuplicatedException
import kr.co.programmers.collabond.shared.exception.InvalidException
import org.slf4j.LoggerFactory
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service

@Service
class OAuth2UserService(
    private val userRepository: UserRepository
) : DefaultOAuth2UserService() {

    private val logger = LoggerFactory.getLogger(OAuth2UserService::class.java)

    @Transactional
    override fun loadUser(userRequest: OAuth2UserRequest): OAuth2User {
        // 기본 정보 가져오기
        val oAuth2User = super.loadUser(userRequest)
        val provider = userRequest.clientRegistration.registrationId

        val oAuth2ResponseDto = when (provider) {
            "google" -> GoogleResponseDto(oAuth2User.attributes)
            "naver" -> NaverResponseDto(oAuth2User.attributes)
            "kakao" -> KakaoResponseDto(oAuth2User.attributes)
            else -> throw InvalidException(ErrorCode.INVALID_PROVIDER)
        }

        // providerId로 사용자 찾기
        val providerId = "${oAuth2ResponseDto.provider}_${oAuth2ResponseDto.providerId}"

        userRepository.findByProviderId(providerId)?.let { user ->
            return toOAuth2UserInfo(user)
        }

        // 이메일 중복 체크
        userRepository.findByEmail(oAuth2ResponseDto.email)?.let {
            throw DuplicatedException(ErrorCode.DUPLICATED_USER_EMAIL)
        }

        val newUser = userRepository.save(
            toUser(oAuth2ResponseDto, Role.ROLE_TMP)
        )

        return toOAuth2UserInfo(newUser)
    }
}