package com.newlecture.resuit.controller;

import com.newlecture.resuit.dto.CompanyVerificationDto.*;
import com.newlecture.resuit.service.CompanyVerificationException;
import com.newlecture.resuit.service.CompanyVerificationService;
import com.newlecture.resuit.service.CurrentUserResolver;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/verifications")
public class CompanyVerificationController {
    private final CompanyVerificationService service;
    private final CurrentUserResolver currentUser;

    @GetMapping("/me")
    public MyStatusResponse getMyStatus(
            @RequestHeader(value = "X-Username", required = false) String username) {
        return service.getMyStatus(currentUser.requireUserId(username));
    }

    @PostMapping("/email")
    public ResponseEntity<EmailResponse> verify(@RequestBody EmailRequest request, Principal principal) {
        return ResponseEntity.ok(service.verify(principal == null ? null : principal.getName(), request));
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> revoke(Principal principal) {
        service.revoke(principal == null ? null : principal.getName());
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(CompanyVerificationException.class)
    public ResponseEntity<ErrorResponse> error(CompanyVerificationException ex) {
        int status = switch (ex.getKind()) {
            case UNAUTHENTICATED -> 401;
            default -> 400;
        };
        return ResponseEntity.status(status).body(new ErrorResponse(ex.getKind().name(), ex.getMessage()));
    }
}
