package kr.co.programmers.collabond.api.user.application

import kr.co.programmers.collabond.api.user.domain.Role
import kr.co.programmers.collabond.api.user.domain.SignUpResponseDto
import kr.co.programmers.collabond.api.user.domain.User
import kr.co.programmers.collabond.api.user.domain.UserResponseDto
import kr.co.programmers.collabond.api.user.domain.UserSignUpRequestDto
import kr.co.programmers.collabond.api.user.domain.UserUpdateRequestDto
import kr.co.programmers.collabond.api.user.infrastructure.UserRepository
import kr.co.programmers.collabond.api.user.interfaces.toResponseDto
import kr.co.programmers.collabond.api.user.interfaces.toSignUpResponseDto
import kr.co.programmers.collabond.core.auth.jwt.TokenService
import kr.co.programmers.collabond.shared.exception.ErrorCode
import kr.co.programmers.collabond.shared.exception.custom.InvalidException
import kr.co.programmers.collabond.shared.exception.custom.NotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val userRepository: UserRepository,
    private val tokenService: TokenService
) {
    @Transactional
    fun signup(providerId: String, dto: UserSignUpRequestDto): SignUpResponseDto {
        val user = userRepository.findByProviderId(providerId)
            ?: throw NotFoundException(ErrorCode.USER_NOT_FOUND)

        if (user.role != Role.ROLE_TMP) {
            throw InvalidException(ErrorCode.INVALID_SIGNUP_REQUEST)
        }

        return user
            .update(nickname = dto.nickname, role = dto.role)
            .let { toSignUpResponseDto(it, tokenService.createAccessToken(providerId, dto.role))}
    }

    @Transactional
    fun update(providerId: String, dto: UserUpdateRequestDto): UserResponseDto =
        findUserByProviderId(providerId)
            .update(nickname = dto.nickname)
            .let {toResponseDto(it)}

    @Transactional
    fun delete(providerId: String) {
        findUserByProviderId(providerId)
            .let(userRepository::delete)
    }

    @Transactional(readOnly = true)
    fun findById(userId: Long): UserResponseDto =
        userRepository.findById(userId)
            .orElseThrow { NotFoundException(ErrorCode.USER_NOT_FOUND) }
            .let { toResponseDto(it) }

    @Transactional(readOnly = true)
    fun findByEmail(email: String): UserResponseDto =
        userRepository.findByEmail(email)
            ?.let { toResponseDto(it) }
            ?: throw NotFoundException(ErrorCode.USER_NOT_FOUND)

    @Transactional(readOnly = true)
    fun findByProviderIdToDto(providerId: String): UserResponseDto =
        toResponseDto(findUserByProviderId(providerId))

    @Transactional
    fun findByProviderId(providerId: String): User =
        findUserByProviderId(providerId)

    @Transactional(readOnly = true)
    fun findAllUsers(): List<UserResponseDto> =
        userRepository.findAll()
            .map { toResponseDto(it) }

    @Transactional
    fun deleteById(userId: Long) {
        userRepository.findById(userId)
            .orElseThrow { NotFoundException(ErrorCode.USER_NOT_FOUND) }
            .let(userRepository::delete)
    }

    private fun findUserByProviderId(providerId: String): User =
        userRepository.findByProviderId(providerId)
            ?: throw NotFoundException(ErrorCode.USER_NOT_FOUND)
}