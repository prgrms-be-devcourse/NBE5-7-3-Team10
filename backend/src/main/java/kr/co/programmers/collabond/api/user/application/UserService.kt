package kr.co.programmers.collabond.api.user.application;

import kr.co.programmers.collabond.api.profile.domain.Profile;
import kr.co.programmers.collabond.api.profile.infrastructure.ProfileRepository;
import kr.co.programmers.collabond.api.user.domain.Role;
import kr.co.programmers.collabond.api.user.domain.User;
import kr.co.programmers.collabond.api.user.domain.dto.UserResponseDto;
import kr.co.programmers.collabond.api.user.domain.dto.UserSignUpRequestDto;
import kr.co.programmers.collabond.api.user.domain.dto.UserUpdateRequestDto;
import kr.co.programmers.collabond.api.user.infrastructure.UserRepository;
import kr.co.programmers.collabond.api.user.interfaces.UserMapper;
import kr.co.programmers.collabond.shared.exception.ErrorCode;
import kr.co.programmers.collabond.shared.exception.custom.InvalidException;
import kr.co.programmers.collabond.shared.exception.custom.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;


    @Transactional
    public UserResponseDto signup(String providerId, UserSignUpRequestDto dto) {
        User user = userRepository.findByProviderId(providerId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

        if(user.getRole() != Role.ROLE_TMP) {
            throw new InvalidException(ErrorCode.INVALID_SIGNUP_REQUEST);
        }

        user.update(null, dto.nickname(), Role.valueOf(dto.role()));
        return UserMapper.toResponseDto(user);
    }

    @Transactional
    public UserResponseDto update(String providerId, UserUpdateRequestDto dto) {
        User user = userRepository.findByProviderId(providerId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

        user.update(null, dto.nickname(), null);
        return UserMapper.toResponseDto(user);
    }

    @Transactional
    public void delete(String providerId) {
        User user = userRepository.findByProviderId(providerId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

        userRepository.delete(user);
    }

    @Transactional(readOnly = true)
    public UserResponseDto findById(Long userId) {
        return userRepository.findById(userId).map(UserMapper::toResponseDto)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public UserResponseDto findByEmail(String email) {
        return userRepository.findByEmail(email).map(UserMapper::toResponseDto)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public UserResponseDto findByProviderIdToDto(String providerId) {
        return userRepository.findByProviderId(providerId).map(UserMapper::toResponseDto)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));
    }

    @Transactional
    public User findByProviderId(String providerId) {
        return userRepository.findByProviderId(providerId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));
    }


    @Transactional(readOnly = true)
    public List<UserResponseDto> findAllUsers() {
        return userRepository.findAll().stream()
                .map(UserMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));
        userRepository.delete(user);
    }

}
