package kr.co.programmers.collabond.api.user.interfaces;

import kr.co.programmers.collabond.api.user.domain.User;
import kr.co.programmers.collabond.api.user.domain.dto.SignUpResponseDto;
import kr.co.programmers.collabond.api.user.domain.dto.UserResponseDto;

public class UserMapper {

    public static UserResponseDto toResponseDto(User user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .role(user.getRole().name())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    public static SignUpResponseDto toSignUpResponseDto(User user, String accessToken) {
        return SignUpResponseDto.builder()
                .nickname(user.getNickname())
                .role(user.getRole().name())
                .accessToken(accessToken)
                .build();
    }
}
