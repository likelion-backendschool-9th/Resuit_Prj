package com.newlecture.resuit.dto;

// 기업 Pro 결제 승인 결과. 바뀐 요금제를 함께 담아 화면이 다시 조회하지 않아도 되게 한다.
public record CompanyProPaymentResultResponse(
		Long paymentId,
		String orderId,
		String product,
		int days,
		int amount,
		String status,
		CompanySubscriptionResponse subscription) {
}
