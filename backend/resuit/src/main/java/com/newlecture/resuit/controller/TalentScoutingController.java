package com.newlecture.resuit.controller;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.newlecture.resuit.dto.CompanyOfferCreateRequest;
import com.newlecture.resuit.dto.CompanyOfferResponse;
import com.newlecture.resuit.dto.CompanySubscriptionRequest;
import com.newlecture.resuit.dto.CompanySubscriptionResponse;
import com.newlecture.resuit.dto.TalentPageResponse;
import com.newlecture.resuit.dto.TalentResumeQuery;
import com.newlecture.resuit.dto.TalentSearchCondition;
import com.newlecture.resuit.dto.resume.CommonResumeDetailResponse;
import com.newlecture.resuit.service.CompanyOfferService;
import com.newlecture.resuit.service.CompanySubscriptionService;
import com.newlecture.resuit.service.TalentScoutingService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1")
public class TalentScoutingController {

	private final TalentScoutingService talentScoutingService;
	private final CompanySubscriptionService subscriptionService;
	private final CompanyOfferService offerService;

	public TalentScoutingController(TalentScoutingService talentScoutingService,
			CompanySubscriptionService subscriptionService, CompanyOfferService offerService) {
		this.talentScoutingService = talentScoutingService;
		this.subscriptionService = subscriptionService;
		this.offerService = offerService;
	}

	@GetMapping("/company/talents")
	public ResponseEntity<TalentPageResponse> getTalents(
			@Valid TalentSearchCondition condition,
			Principal principal) {
		return ResponseEntity.ok(talentScoutingService.getTalents(condition, principal.getName()));
	}

	@GetMapping("/company/talents/{userId}/resumes/{c_resumeId}")
	public ResponseEntity<CommonResumeDetailResponse> getPublicResume(
			@PathVariable UUID userId,
			@PathVariable Long c_resumeId,
			Principal principal) {
		return ResponseEntity.ok(talentScoutingService.getPublicResume(
				new TalentResumeQuery(userId, c_resumeId, principal.getName())));
	}

	@PostMapping("/company/offers")
	public ResponseEntity<CompanyOfferResponse> createCompanyOffer(
			@Valid @RequestBody CompanyOfferCreateRequest request,
			Principal principal) {
		return ResponseEntity.status(HttpStatus.CREATED).body(offerService.send(principal.getName(), request));
	}

	// 보낸 제안 조회: GET /api/v1/company/offers. 기업 회원이 보낸 제안을 최신순으로 돌려준다.
	@GetMapping("/company/offers")
	public ResponseEntity<List<CompanyOfferResponse>> getSentOffers(
			Principal principal) {
		return ResponseEntity.ok(offerService.getSent(principal.getName()));
	}

	@GetMapping("/users/me/offers")
	public ResponseEntity<List<CompanyOfferResponse>> getReceivedOffers(
			Principal principal) {
		return ResponseEntity.ok(offerService.getReceived(principal.getName()));
	}

	@GetMapping("/company/subscription")
	public ResponseEntity<CompanySubscriptionResponse> getCompanySubscription(
			Principal principal) {
		return ResponseEntity.ok(subscriptionService.getMySubscription(principal.getName()));
	}

	@PutMapping("/company/subscription")
	public ResponseEntity<CompanySubscriptionResponse> updateCompanySubscription(
			@Valid @RequestBody CompanySubscriptionRequest request,
			Principal principal) {
		return ResponseEntity.ok(subscriptionService.updateMySubscription(principal.getName(), request));
	}
}
