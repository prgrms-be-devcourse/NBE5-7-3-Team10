package kr.co.programmers.collabond.core.auth.jwt

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import kr.co.programmers.collabond.api.user.domain.RefreshToken
import kr.co.programmers.collabond.api.user.domain.Role
import kr.co.programmers.collabond.api.user.domain.TokenStatus
import kr.co.programmers.collabond.api.user.infrastructure.RefreshTokenRepository
import kr.co.programmers.collabond.api.user.infrastructure.UserRepository
import kr.co.programmers.collabond.shared.exception.ErrorCode
import kr.co.programmers.collabond.shared.exception.custom.ExpiredException
import kr.co.programmers.collabond.shared.exception.custom.InvalidException
import kr.co.programmers.collabond.shared.exception.custom.NotFoundException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.*

@Service
class TokenService(
    private val userRepository: UserRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    @Value("\${custom.jwt.token-validity-time.access}")
    private val accessTokenValiditySeconds: Long,
    @Value("\${custom.jwt.token-validity-time.refresh}")
    private val refreshTokenValiditySeconds: Long,
    @Value("\${custom.jwt.secret}")
    private val secretKey: String
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun issueTokens(providerId: String, role: Role): LoginTokenResponseDto {
        val user = userRepository.findByProviderId(providerId)
            ?: throw NotFoundException(ErrorCode.USER_NOT_FOUND)

        val accessToken = createAccessToken(providerId, role)

        refreshTokenRepository.findByUserAndStatus(user, TokenStatus.VALID)?.let {
            return toLoginTokenResponseDto(accessToken, it.token, user)
        }

        val refreshToken = createRefreshToken(providerId, role)

        val newRefreshToken = RefreshToken(
            token = refreshToken,
            user = user
        )
        refreshTokenRepository.save(newRefreshToken)

        return toLoginTokenResponseDto(accessToken, refreshToken, user)
    }

    fun createAccessToken(providerId: String, role: Role): String =
        createToken(providerId, role, accessTokenValiditySeconds)

    fun createRefreshToken(providerId: String, role: Role): String =
        createToken(providerId, role, refreshTokenValiditySeconds)

    fun createToken(providerId: String, role: Role, validitySeconds: Long): String {
        val now = Date()
        val expirationDate = Date(now.time + validitySeconds * 1000)

        return Jwts.builder()
            .subject(providerId)
            .claim("role", role)
            .issuedAt(now)
            .expiration(expirationDate)
            .signWith(getSignKey(), Jwts.SIG.HS512)
            .compact()
    }

    fun refreshAccessToken(refreshToken: String): AccessTokenResponseDto {
        logger.info("refreshToken = $refreshToken")

        val token = refreshTokenRepository.findByToken(refreshToken)
            ?: throw NotFoundException(ErrorCode.TOKEN_NOT_FOUND)

        if (token.status != TokenStatus.VALID) {
            throw InvalidException(ErrorCode.INVALID_TOKEN)
        }

        if (!validate(refreshToken)) {
            token.updateStatus(TokenStatus.EXPIRED)
            refreshTokenRepository.save(token)
            throw ExpiredException(ErrorCode.EXPIRED_REFRESH_TOKEN)
        }

        val tokenBodyDto = parseJwt(refreshToken)
        val newAccessToken = createAccessToken(tokenBodyDto.providerId, tokenBodyDto.role)
        return AccessTokenResponseDto(newAccessToken)
    }

    fun logout(refreshToken: String) {
        val token = refreshTokenRepository.findByToken(refreshToken)
            ?: throw NotFoundException(ErrorCode.TOKEN_NOT_FOUND)

        token.updateStatus(TokenStatus.LOGGED_OUT)
        refreshTokenRepository.save(token)
    }

    fun validate(token: String): Boolean = try {
        Jwts.parser()
            .verifyWith(getSignKey())
            .build()
            .parseSignedClaims(token)
        true
    } catch (e: Exception) {
        logger.debug("Invalid JWT token: ${e.message}")
        false
    }

    fun parseJwt(token: String): TokenBodyDto {
        val parsed = Jwts.parser()
            .verifyWith(getSignKey())
            .build()
            .parseSignedClaims(token)

        val sub = parsed.payload.subject
        val role = parsed.payload["role"].toString()

        return toTokenBodyDto(sub, role)
    }

    private fun getSignKey() =
        Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey))
}