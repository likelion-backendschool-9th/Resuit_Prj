package com.newlecture.resuit.dto;

import java.time.LocalDateTime;

import com.newlecture.resuit.entity.CreditWithdrawal;

// 환전 결과. 계좌번호는 뒤 4자리만 보여 준다.
public record CreditWithdrawalResponse(
		Long id,
		int credits,
		int amountKrw,
		int feeKrw,
		String bankName,
		String accountNumber,
		String accountHolder,
		int balance,
		int withdrawable,
		LocalDateTime createdAt) {

	public static CreditWithdrawalResponse of(CreditWithdrawal withdrawal, int balance, int withdrawable) {
		return new CreditWithdrawalResponse(
				withdrawal.getId(),
				withdrawal.getCredits(),
				withdrawal.getAmountKrw(),
				withdrawal.getFeeKrw(),
				withdrawal.getBankName(),
				mask(withdrawal.getAccountNumber()),
				withdrawal.getAccountHolder(),
				balance,
				withdrawable,
				withdrawal.getCreatedAt());
	}

	private static String mask(String accountNumber) {
		int keep = Math.min(4, accountNumber.length());
		return "*".repeat(accountNumber.length() - keep) + accountNumber.substring(accountNumber.length() - keep);
	}
}
