package com.newlecture.resuit.dto;

public record CreditPaymentOrderResponse(
		Long paymentId,
		String orderId,
		String orderName,
		int amount,
		int credits,
		String clientKey,
		String customerKey) {
}
