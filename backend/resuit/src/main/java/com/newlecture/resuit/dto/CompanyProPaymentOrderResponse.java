package com.newlecture.resuit.dto;

public record CompanyProPaymentOrderResponse(
		Long paymentId,
		String orderId,
		String orderName,
		int amount,
		String product,
		int days,
		String clientKey,
		String customerKey) {
}
