package com.newlecture.resuit.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// 기존 소셜 회원의 첫 비밀번호 설정. 아이디와 소유자는 세션에서 결정한다.
public record PasswordSetupRequest(@NotBlank @Size(min = 8, max = 72) String password) {}
