package com.newlecture.resuit.controller;

import java.security.Principal;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.newlecture.resuit.dto.CreditBalanceResponse;
import com.newlecture.resuit.dto.CreditTransactionResponse;
import com.newlecture.resuit.dto.MentorDetailResponse;
import com.newlecture.resuit.dto.MentorSummary;
import com.newlecture.resuit.dto.PaidFeedbackCreateRequest;
import com.newlecture.resuit.dto.PaidFeedbackDetailResponse;
import com.newlecture.resuit.dto.PaidFeedbackResponseCommand;
import com.newlecture.resuit.dto.PaidFeedbackResponseCreateRequest;
import com.newlecture.resuit.dto.PaidFeedbackResponseSummary;
import com.newlecture.resuit.dto.PaidFeedbackSearchCondition;
import com.newlecture.resuit.dto.PaidFeedbackSummary;
import com.newlecture.resuit.service.CreditService;
import com.newlecture.resuit.service.MentorService;
import com.newlecture.resuit.service.PaidFeedbackService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1")
public class PaidFeedbackController {

	private final MentorService mentorService;
	private final PaidFeedbackService paidFeedbackService;
	private final CreditService creditService;

	public PaidFeedbackController(
			MentorService mentorService,
			PaidFeedbackService paidFeedbackService,
			CreditService creditService) {
		this.mentorService = mentorService;
		this.paidFeedbackService = paidFeedbackService;
		this.creditService = creditService;
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
	public ResponseEntity<PaidFeedbackSummary> createPaidFeedbackRequest(
			@Valid @RequestBody PaidFeedbackCreateRequest request,
			Principal principal) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(paidFeedbackService.create(request, principal.getName()));
	}

	@GetMapping("/paid-feedback-requests")
	public ResponseEntity<Page<PaidFeedbackSummary>> getPaidFeedbackRequests(
			@Valid PaidFeedbackSearchCondition condition,
			Principal principal) {
		return ResponseEntity.ok(paidFeedbackService.getRequests(condition, principal.getName()));
	}

	@GetMapping("/paid-feedback-requests/{id}")
	public ResponseEntity<PaidFeedbackDetailResponse> getPaidFeedbackRequest(
			@PathVariable Long id,
			Principal principal) {
		return ResponseEntity.ok(paidFeedbackService.getRequest(id, principal.getName()));
	}

	@PostMapping("/paid-feedback-requests/{id}/responses")
	public ResponseEntity<PaidFeedbackResponseSummary> createPaidFeedbackResponse(
			@PathVariable Long id,
			@Valid @RequestBody PaidFeedbackResponseCreateRequest request,
			Principal principal) {
		return ResponseEntity.status(HttpStatus.CREATED).body(paidFeedbackService.createResponse(
				new PaidFeedbackResponseCommand(id, principal.getName(), request.content())));
	}

	@GetMapping("/credits/balance")
	public ResponseEntity<CreditBalanceResponse> getCreditBalance(Principal principal) {
		return ResponseEntity.ok(creditService.getBalance(principal.getName()));
	}

	@GetMapping("/credits/transactions")
	public ResponseEntity<Page<CreditTransactionResponse>> getCreditTransactions(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size,
			Principal principal) {
		return ResponseEntity.ok(creditService.getTransactions(principal.getName(), PageRequest.of(page, size)));
	}

	@PostMapping("/credit-payments")
	public ResponseEntity<Void> createCreditPayment(
			@RequestBody(required = false) Map<String, Object> paymentRequest) {
		return ResponseEntity.ok().build();
	}
}
