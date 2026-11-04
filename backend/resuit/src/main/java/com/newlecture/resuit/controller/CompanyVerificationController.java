package com.newlecture.resuit.controller;

import com.newlecture.resuit.dto.CompanyVerificationDto.*;
import com.newlecture.resuit.service.CompanyVerificationService;
import com.newlecture.resuit.service.CompanyVerificationException;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/verifications")
public class CompanyVerificationController {
    private final CompanyVerificationService service;

    @PostMapping("/email")
    public ResponseEntity<EmailResponse> send(@RequestBody EmailRequest request, Principal principal) {
        return ResponseEntity.ok(service.send(principal == null ? null : principal.getName(), request));
    }

    @PostMapping("/email/confirm")
    public ResponseEntity<ConfirmResponse> confirm(@RequestBody ConfirmRequest request, Principal principal) {
        return ResponseEntity.ok(service.confirm(principal == null ? null : principal.getName(), request));
    }

    @ExceptionHandler(CompanyVerificationException.class)
    public ResponseEntity<ErrorResponse> error(CompanyVerificationException ex) {
        int status = switch (ex.getKind()) {
            case UNAUTHENTICATED -> 401;
            case NOT_FOUND -> 404;
            case NOT_CONFIGURED, DELIVERY_FAILED -> 503;
            case RESEND_TOO_SOON, ATTEMPTS_EXCEEDED -> 429;
            case EXPIRED -> 410;
            case ALREADY_VERIFIED -> 409;
            default -> 400;
        };
        return ResponseEntity.status(status).body(new ErrorResponse(ex.getKind().name(), ex.getMessage()));
    }
}
