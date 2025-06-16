package kr.co.programmers.collabond.api.user.domain.dto;

import lombok.Builder;

@Builder
public record SignUpResponseDto(String nickname, String role, String accessToken) { }
