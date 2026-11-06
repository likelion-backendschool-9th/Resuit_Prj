package com.newlecture.resuit.service;

public class CompanyVerificationException extends RuntimeException {
    public enum Kind { UNAUTHENTICATED, INVALID_INPUT, DOMAIN_NOT_ALLOWED }
    private final Kind kind;
    public CompanyVerificationException(Kind kind, String message) { super(message); this.kind = kind; }
    public Kind getKind() { return kind; }
}
