package com.newlecture.resuit.controller;

import java.util.Map;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class TalentScoutingController {

	@GetMapping("/company/talents")
	public ResponseEntity<Void> getTalents(
			@RequestParam(required = false) String field,
			@RequestParam(required = false) String badge,
			@RequestParam(required = false) Integer streak,
			@RequestParam(required = false) String skills,
			@RequestParam(required = false) String career,
			@RequestParam(required = false) Integer minContrib,
			@RequestParam(required = false) Integer minPosts,
			@RequestParam(required = false) String region,
			@RequestParam(required = false) Integer page,
			@RequestParam(required = false) Integer size) {
		return ResponseEntity.ok().build();
	}

	@GetMapping("/company/talents/{userId}/resumes/{c_resumeId}")
	public ResponseEntity<Void> getPublicResume(
			@PathVariable UUID userId,
			@PathVariable Long c_resumeId) {
		return ResponseEntity.ok().build();
	}

	@PostMapping("/company/offers")
	public ResponseEntity<Void> createCompanyOffer(
			@RequestBody(required = false) Map<String, Object> offerRequest) {
		return ResponseEntity.ok().build();
	}

	@GetMapping("/users/me/offers")
	public ResponseEntity<Void> getReceivedOffers() {
		return ResponseEntity.ok().build();
	}

	@GetMapping("/company/subscription")
	public ResponseEntity<Void> getCompanySubscription() {
		return ResponseEntity.ok().build();
	}

	@PutMapping("/company/subscription")
	public ResponseEntity<Void> updateCompanySubscription(
			@RequestBody(required = false) Map<String, Object> subscriptionRequest) {
		return ResponseEntity.ok().build();
	}
}
