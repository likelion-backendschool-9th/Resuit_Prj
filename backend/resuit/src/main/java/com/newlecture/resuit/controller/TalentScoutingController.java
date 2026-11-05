package com.newlecture.resuit.controller;

import java.security.Principal;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.newlecture.resuit.dto.TalentSearchCondition;
import com.newlecture.resuit.dto.TalentSummary;
import com.newlecture.resuit.service.TalentScoutingService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1")
public class TalentScoutingController {

	private final TalentScoutingService talentScoutingService;

	public TalentScoutingController(TalentScoutingService talentScoutingService) {
		this.talentScoutingService = talentScoutingService;
	}

	@GetMapping("/company/talents")
	public ResponseEntity<Page<TalentSummary>> getTalents(
			@Valid TalentSearchCondition condition,
			Principal principal) {
		return ResponseEntity.ok(talentScoutingService.getTalents(condition, principal.getName()));
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
