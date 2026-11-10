package com.newlecture.resuit.dto;

import java.time.LocalDateTime;

import com.newlecture.resuit.entity.CreditTransaction;

public record CreditTransactionResponse(
		Long id,
		String type,
		int amount,
		int balanceAfter,
		Long paidFeedbackRequestId,
		Long creditPaymentId,
		Long creditWithdrawalId,
		LocalDateTime createdAt) {

	public static CreditTransactionResponse from(CreditTransaction transaction) {
		return new CreditTransactionResponse(
				transaction.getId(),
				transaction.getType(),
				transaction.getAmount(),
				transaction.getBalanceAfter(),
				transaction.getPaidFeedbackRequest() == null ? null : transaction.getPaidFeedbackRequest().getId(),
				transaction.getCreditPayment() == null ? null : transaction.getCreditPayment().getId(),
				transaction.getCreditWithdrawal() == null ? null : transaction.getCreditWithdrawal().getId(),
				transaction.getCreatedAt());
	}
}
