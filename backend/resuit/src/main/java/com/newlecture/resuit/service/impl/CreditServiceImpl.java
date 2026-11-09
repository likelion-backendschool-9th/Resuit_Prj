package com.newlecture.resuit.service.impl;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.newlecture.resuit.dto.CreditBalanceResponse;
import com.newlecture.resuit.dto.CreditTransactionResponse;
import com.newlecture.resuit.entity.CreditPayment;
import com.newlecture.resuit.entity.CreditTransaction;
import com.newlecture.resuit.entity.PaidFeedbackRequest;
import com.newlecture.resuit.entity.User;
import com.newlecture.resuit.repository.CreditTransactionRepository;
import com.newlecture.resuit.repository.UserRepository;
import com.newlecture.resuit.service.CreditService;

@Service
@Transactional(readOnly = true)
public class CreditServiceImpl implements CreditService {

	private final CreditTransactionRepository creditTransactionRepository;
	private final UserRepository userRepository;

	public CreditServiceImpl(
			CreditTransactionRepository creditTransactionRepository,
			UserRepository userRepository) {
		this.creditTransactionRepository = creditTransactionRepository;
		this.userRepository = userRepository;
	}

	@Override
	public CreditBalanceResponse getBalance(String username) {
		return new CreditBalanceResponse(currentBalance(findUserId(username)));
	}

	@Override
	public Page<CreditTransactionResponse> getTransactions(String username, Pageable pageable) {
		return creditTransactionRepository.findByUser_IdOrderByIdDesc(findUserId(username), pageable)
				.map(CreditTransactionResponse::from);
	}

	@Override
	@Transactional
	public void use(PaidFeedbackRequest request) {
		User user = lockUser(request.getRequester().getId());
		int balance = currentBalance(user.getId());
		if (balance < request.getCredits()) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "크레딧이 부족합니다.");
		}
		creditTransactionRepository.save(CreditTransaction.builder()
				.user(user)
				.type("USE")
				.amount(-request.getCredits())
				.balanceAfter(balance - request.getCredits())
				.paidFeedbackRequest(request)
				.build());
	}

	@Override
	@Transactional
	public void earn(PaidFeedbackRequest request) {
		User user = lockUser(request.getMentor().getUserId());
		creditTransactionRepository.save(CreditTransaction.builder()
				.user(user)
				.type("EARN")
				.amount(request.getCredits())
				.balanceAfter(currentBalance(user.getId()) + request.getCredits())
				.paidFeedbackRequest(request)
				.build());
	}

	// 승인된 결제만큼 크레딧을 충전한다. 결제 승인과 같은 트랜잭션에서 불린다(설계 문서 기준).
	@Override
	@Transactional
	public int charge(CreditPayment payment) {
		User user = lockUser(payment.getUser().getId());
		int balanceAfter = currentBalance(user.getId()) + payment.getCredits();
		creditTransactionRepository.save(CreditTransaction.builder()
				.user(user)
				.type("CHARGE")
				.amount(payment.getCredits())
				.balanceAfter(balanceAfter)
				.creditPayment(payment)
				.build());
		return balanceAfter;
	}

	private UUID findUserId(String username) {
		return userRepository.findByUsername(username)
				.map(User::getId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
	}

	private User lockUser(UUID userId) {
		return userRepository.findWithLockById(userId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
	}

	private int currentBalance(UUID userId) {
		return creditTransactionRepository.findFirstByUser_IdOrderByIdDesc(userId)
				.map(CreditTransaction::getBalanceAfter)
				.orElse(0);
	}
}
