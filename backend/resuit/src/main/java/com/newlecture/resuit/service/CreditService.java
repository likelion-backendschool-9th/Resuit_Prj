package com.newlecture.resuit.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.newlecture.resuit.dto.CreditBalanceResponse;
import com.newlecture.resuit.dto.CreditTransactionResponse;
import com.newlecture.resuit.entity.CreditPayment;
import com.newlecture.resuit.entity.PaidFeedbackRequest;

public interface CreditService {

	CreditBalanceResponse getBalance(String username);

	Page<CreditTransactionResponse> getTransactions(String username, Pageable pageable);

	void use(PaidFeedbackRequest request);

	void earn(PaidFeedbackRequest request);

	int charge(CreditPayment payment);
}
