package com.newlecture.resuit.dto;

import java.time.LocalDateTime;

public final class CompanyVerificationDto {
    private CompanyVerificationDto() {}
    public record EmailRequest(String email) {}
    public record ConfirmRequest(Long verificationId, String code) {}
    public record EmailResponse(Long verificationId, LocalDateTime expiresAt, int resendAfterSeconds) {}
    public record ConfirmResponse(Long verificationId, String email, LocalDateTime verifiedAt) {}
    public record ErrorResponse(String code, String message) {}
}
