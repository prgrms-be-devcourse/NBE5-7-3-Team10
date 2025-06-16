package kr.co.programmers.collabond.core.auth.jwt;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequestDto(
        @NotBlank
        String refreshToken) {}
