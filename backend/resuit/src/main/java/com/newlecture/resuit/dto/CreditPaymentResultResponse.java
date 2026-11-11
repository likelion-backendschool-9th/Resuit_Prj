package com.newlecture.resuit.dto;

public record CreditPaymentResultResponse(
		Long paymentId,
		String orderId,
		int credits,
		int amount,
		String status,
		int balance) {
}
