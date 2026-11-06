package com.newlecture.resuit.dto;

import java.time.LocalDateTime;

public final class CompanyVerificationDto {
    private CompanyVerificationDto() {}
    public record EmailRequest(String email) {}
    public record EmailResponse(Long verificationId, String email, LocalDateTime verifiedAt) {}
    public record MyStatusResponse(String type, String status, String email, LocalDateTime verifiedAt) {}
    public record ErrorResponse(String code, String message) {}
}
