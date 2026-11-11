package com.newlecture.resuit.controller;

import java.security.Principal;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.newlecture.resuit.dto.CompanyProPaymentCreateRequest;
import com.newlecture.resuit.dto.CompanyProPaymentOrderResponse;
import com.newlecture.resuit.dto.CompanyProPaymentResultResponse;
import com.newlecture.resuit.dto.CreditPaymentConfirmRequest;
import com.newlecture.resuit.service.CompanyProPaymentService;

import jakarta.validation.Valid;

// 기업 Pro 이용권 결제 API. 요금제 조회·체험은 TalentScoutingController의 /company/subscription에 있다.
@RestController
@RequestMapping("/api/v1/company/pro-payments")
public class CompanyProPaymentController {

	private final CompanyProPaymentService proPaymentService;

	public CompanyProPaymentController(CompanyProPaymentService proPaymentService) {
		this.proPaymentService = proPaymentService;
	}

	// 이용권 주문: POST /api/v1/company/pro-payments  {"product":"PRO_1M"}
	@PostMapping
	public ResponseEntity<CompanyProPaymentOrderResponse> create(
			@Valid @RequestBody CompanyProPaymentCreateRequest request, Principal principal) {
		return ResponseEntity.status(HttpStatus.CREATED).body(proPaymentService.create(principal.getName(), request));
	}

	// 결제 승인: POST /api/v1/company/pro-payments/confirm  {"paymentKey":"...","orderId":"resuit-pro-1","amount":49000}
	// 요청 모양이 크레딧 결제 승인과 같아 CreditPaymentConfirmRequest를 재사용한다.
	@PostMapping("/confirm")
	public ResponseEntity<CompanyProPaymentResultResponse> confirm(
			@Valid @RequestBody CreditPaymentConfirmRequest request, Principal principal) {
		return ResponseEntity.ok(proPaymentService.confirm(principal.getName(), request));
	}
}
