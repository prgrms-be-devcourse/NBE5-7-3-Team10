package kr.co.programmers.collabond.core.auth.oauth2

import kr.co.programmers.collabond.api.user.domain.Role
import kr.co.programmers.collabond.api.user.domain.User
import kr.co.programmers.collabond.core.auth.jwt.TokenBodyDto

fun toOAuth2UserInfo(user: User): OAuth2UserInfo =
    OAuth2UserInfo(
        username = user.providerId ?: "",
        email = user.email,
        _name = user.nickname,
        role = user.role
    )

fun toOAuth2UserInfo(tokenBodyDto: TokenBodyDto): OAuth2UserInfo =
    OAuth2UserInfo(
        username = tokenBodyDto.providerId,
        role = tokenBodyDto.role
    )

fun toUser(oAuth2ResponseDto: OAuth2ResponseDto, role: Role): User =
    User(
        email = oAuth2ResponseDto.email,
        nickname = oAuth2ResponseDto.name,
        role = role,
        providerId = "${oAuth2ResponseDto.provider}_${oAuth2ResponseDto.providerId}"
    )