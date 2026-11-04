package com.newlecture.resuit.controller;

import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.newlecture.resuit.dto.MentorDetailResponse;
import com.newlecture.resuit.dto.MentorSummary;
import com.newlecture.resuit.service.MentorService;

@RestController
@RequestMapping("/api/v1")
public class PaidFeedbackController {

	private final MentorService mentorService;

	public PaidFeedbackController(MentorService mentorService) {
		this.mentorService = mentorService;
	}

	@GetMapping("/mentors")
	public ResponseEntity<Page<MentorSummary>> getMentors(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		return ResponseEntity.ok(mentorService.getMentors(PageRequest.of(page, size)));
	}

	@GetMapping("/mentors/{id}")
	public ResponseEntity<MentorDetailResponse> getMentor(@PathVariable UUID id) {
		return ResponseEntity.ok(mentorService.getMentor(id));
	}

	@PostMapping("/paid-feedback-requests")
	public ResponseEntity<Void> createPaidFeedbackRequest(
			@RequestBody(required = false) Map<String, Object> feedbackRequest) {
		return ResponseEntity.ok().build();
	}

	@GetMapping("/paid-feedback-requests")
	public ResponseEntity<Void> getPaidFeedbackRequests(
			@RequestParam(required = false) String status) {
		return ResponseEntity.ok().build();
	}

	@GetMapping("/paid-feedback-requests/{id}")
	public ResponseEntity<Void> getPaidFeedbackRequest(@PathVariable Long id) {
		return ResponseEntity.ok().build();
	}

	@PostMapping("/paid-feedback-requests/{id}/responses")
	public ResponseEntity<Void> createPaidFeedbackResponse(
			@PathVariable Long id,
			@RequestBody(required = false) Map<String, Object> responseRequest) {
		return ResponseEntity.ok().build();
	}

	@GetMapping("/credits/balance")
	public ResponseEntity<Void> getCreditBalance() {
		return ResponseEntity.ok().build();
	}

	@GetMapping("/credits/transactions")
	public ResponseEntity<Void> getCreditTransactions() {
		return ResponseEntity.ok().build();
	}

	@PostMapping("/credit-payments")
	public ResponseEntity<Void> createCreditPayment(
			@RequestBody(required = false) Map<String, Object> paymentRequest) {
		return ResponseEntity.ok().build();
	}
}
