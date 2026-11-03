package com.newlecture.resuit.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/verifications")
public class CompanyVerificationController {

	@PostMapping("/email")
	public ResponseEntity<Void> sendCompanyEmailVerification(
			@RequestBody(required = false) Map<String, Object> emailVerificationRequest) {
		return ResponseEntity.ok().build();
	}

	@PostMapping("/email/confirm")
	public ResponseEntity<Void> confirmCompanyEmailVerification(
			@RequestBody(required = false) Map<String, Object> confirmationRequest) {
		return ResponseEntity.ok().build();
	}
}
