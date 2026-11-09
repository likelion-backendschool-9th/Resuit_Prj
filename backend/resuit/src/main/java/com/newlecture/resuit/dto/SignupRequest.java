package com.newlecture.resuit.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SignupRequest(
        @NotBlank @Size(min = 3, max = 50)
        @Pattern(regexp = "[a-zA-Z0-9_-]+") String username,
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Size(min = 8, max = 72) String password,
        @NotBlank @Size(max = 50) String nickname,
        // 가입 종류. COMPANY면 기업 회원(role=COMPANY)으로 가입하고 nickname을 회사명으로 쓴다. 비우면 개인 회원이다.
        @Pattern(regexp = "USER|COMPANY") String accountType) {}
