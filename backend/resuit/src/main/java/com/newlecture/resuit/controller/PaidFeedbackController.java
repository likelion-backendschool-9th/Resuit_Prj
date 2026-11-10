package com.newlecture.resuit.controller;

import java.security.Principal;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.newlecture.resuit.dto.CreditBalanceResponse;
import com.newlecture.resuit.dto.CreditPaymentConfirmRequest;
import com.newlecture.resuit.dto.CreditPaymentCreateRequest;
import com.newlecture.resuit.dto.CreditPaymentOrderResponse;
import com.newlecture.resuit.dto.CreditPaymentResultResponse;
import com.newlecture.resuit.dto.CreditTransactionResponse;
import com.newlecture.resuit.dto.MentorDetailResponse;
import com.newlecture.resuit.dto.MentorProfileRequest;
import com.newlecture.resuit.dto.MyMentorResponse;
import com.newlecture.resuit.dto.MentorSummary;
import com.newlecture.resuit.dto.PaidFeedbackCreateRequest;
import com.newlecture.resuit.dto.PaidFeedbackDetailResponse;
import com.newlecture.resuit.dto.PaidFeedbackResponseCommand;
import com.newlecture.resuit.dto.PaidFeedbackResponseCreateRequest;
import com.newlecture.resuit.dto.PaidFeedbackResponseSummary;
import com.newlecture.resuit.dto.PaidFeedbackSearchCondition;
import com.newlecture.resuit.dto.PaidFeedbackSummary;
import com.newlecture.resuit.dto.resume.ResumePreviewResponse;
import com.newlecture.resuit.service.CreditPaymentService;
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
	private final CreditPaymentService creditPaymentService;

	public PaidFeedbackController(
			MentorService mentorService,
			PaidFeedbackService paidFeedbackService,
			CreditService creditService,
			CreditPaymentService creditPaymentService) {
		this.mentorService = mentorService;
		this.paidFeedbackService = paidFeedbackService;
		this.creditService = creditService;
		this.creditPaymentService = creditPaymentService;
	}

	@GetMapping("/mentors")
	public ResponseEntity<Page<MentorSummary>> getMentors(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		return ResponseEntity.ok(mentorService.getMentors(PageRequest.of(page, size)));
	}

	// 내 현직자 정보: GET /api/v1/users/me/mentor. 현직자 인증 상태와 직무·경력·가격을 돌려준다.
	@GetMapping("/users/me/mentor")
	public ResponseEntity<MyMentorResponse> getMyMentor(Principal principal) {
		return ResponseEntity.ok(mentorService.getMyMentor(principal.getName()));
	}

	// 현직자 정보 저장: PUT /api/v1/users/me/mentor  {"jobTitle":"백엔드 개발자","priceCredits":50}
	@PutMapping("/users/me/mentor")
	public ResponseEntity<MyMentorResponse> saveMyMentor(
			@Valid @RequestBody MentorProfileRequest request, Principal principal) {
		return ResponseEntity.ok(mentorService.saveMyMentor(principal.getName(), request));
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

	// 요청에 붙은 이력서: GET /api/v1/paid-feedback-requests/{id}/resume. 요청자와 그 요청의 현직자만 볼 수 있다.
	@GetMapping("/paid-feedback-requests/{id}/resume")
	public ResponseEntity<ResumePreviewResponse> getPaidFeedbackResume(@PathVariable Long id, Principal principal) {
		return ResponseEntity.ok(paidFeedbackService.getRequestResume(id, principal.getName()));
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
	public ResponseEntity<CreditPaymentOrderResponse> createCreditPayment(
			@Valid @RequestBody CreditPaymentCreateRequest request,
			Principal principal) {
		return ResponseEntity.status(HttpStatus.CREATED).body(creditPaymentService.create(principal.getName(), request));
	}

	@PostMapping("/credit-payments/confirm")
	public ResponseEntity<CreditPaymentResultResponse> confirmCreditPayment(
			@Valid @RequestBody CreditPaymentConfirmRequest request,
			Principal principal) {
		return ResponseEntity.ok(creditPaymentService.confirm(principal.getName(), request));
	}
}
