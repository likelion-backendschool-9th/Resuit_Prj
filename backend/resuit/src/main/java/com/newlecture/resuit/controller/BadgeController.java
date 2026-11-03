package com.newlecture.resuit.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class BadgeController {

	@GetMapping("/verifications/me")
	public ResponseEntity<Void> getMyVerificationStatus() {
		return ResponseEntity.ok().build();
	}
}
