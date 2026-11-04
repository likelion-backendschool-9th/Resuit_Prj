package com.newlecture.resuit.service;

public class CompanyVerificationException extends RuntimeException {
    public enum Kind { UNAUTHENTICATED, INVALID_INPUT, DOMAIN_NOT_ALLOWED, NOT_CONFIGURED,
        RESEND_TOO_SOON, NOT_FOUND, EXPIRED, ATTEMPTS_EXCEEDED, CODE_MISMATCH, ALREADY_VERIFIED, DELIVERY_FAILED }
    private final Kind kind;
    public CompanyVerificationException(Kind kind, String message) { super(message); this.kind = kind; }
    public Kind getKind() { return kind; }
}
