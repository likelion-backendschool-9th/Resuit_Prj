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
import com.newlecture.resuit.dto.CreditWithdrawalCreateRequest;
import com.newlecture.resuit.dto.CreditWithdrawalResponse;
import com.newlecture.resuit.entity.CreditPayment;
import com.newlecture.resuit.entity.CreditTransaction;
import com.newlecture.resuit.entity.CreditWithdrawal;
import com.newlecture.resuit.entity.PaidFeedbackRequest;
import com.newlecture.resuit.entity.User;
import com.newlecture.resuit.repository.CreditTransactionRepository;
import com.newlecture.resuit.repository.CreditWithdrawalRepository;
import com.newlecture.resuit.repository.UserRepository;
import com.newlecture.resuit.service.CreditService;

@Service
@Transactional(readOnly = true)
public class CreditServiceImpl implements CreditService {

	// 환전할 때 1크레딧당 받는 금액(원). 충전가 100원에서 수수료 20%를 뺀 값이다.
	private static final int KRW_PER_CREDIT_WITHDRAW = 80;
	private static final int FEE_PER_CREDIT = 20;

	private final CreditTransactionRepository creditTransactionRepository;
	private final CreditWithdrawalRepository creditWithdrawalRepository;
	private final UserRepository userRepository;

	public CreditServiceImpl(
			CreditTransactionRepository creditTransactionRepository,
			CreditWithdrawalRepository creditWithdrawalRepository,
			UserRepository userRepository) {
		this.creditTransactionRepository = creditTransactionRepository;
		this.creditWithdrawalRepository = creditWithdrawalRepository;
		this.userRepository = userRepository;
	}

	@Override
	public CreditBalanceResponse getBalance(String username) {
		UUID userId = findUserId(username);
		int balance = currentBalance(userId);
		return new CreditBalanceResponse(balance, withdrawable(userId, balance));
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

	// 적립 크레딧을 현금으로 환전한다. 관리자 승인 없이 바로 완료되고, 내역에 WITHDRAW(음수) 한 줄이 남는다.
	@Override
	@Transactional
	public CreditWithdrawalResponse withdraw(String username, CreditWithdrawalCreateRequest request) {
		// 같은 회원의 크레딧 변경은 회원 행을 잠가 하나씩 처리한다. 두 번 눌러도 환전 가능 크레딧을 넘지 않는다.
		User user = lockUser(findUserId(username));
		int balance = currentBalance(user.getId());
		int withdrawable = withdrawable(user.getId(), balance);
		int credits = request.credits();
		if (credits <= 0) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "환전할 크레딧을 1 이상 입력해 주세요.");
		}
		String accountNumber = request.accountNumber().replaceAll("[^0-9]", "");
		if (accountNumber.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "계좌번호를 숫자로 입력해 주세요.");
		}
		if (credits > withdrawable) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "환전할 수 있는 크레딧이 부족합니다.");
		}
		CreditWithdrawal withdrawal = creditWithdrawalRepository.save(CreditWithdrawal.builder()
				.user(user)
				.credits(credits)
				.amountKrw(credits * KRW_PER_CREDIT_WITHDRAW)
				.feeKrw(credits * FEE_PER_CREDIT)
				.bankName(request.bankName().trim())
				.accountNumber(accountNumber)
				.accountHolder(request.accountHolder().trim())
				.build());
		creditTransactionRepository.save(CreditTransaction.builder()
				.user(user)
				.type("WITHDRAW")
				.amount(-credits)
				.balanceAfter(balance - credits)
				.creditWithdrawal(withdrawal)
				.build());
		return CreditWithdrawalResponse.of(withdrawal, balance - credits, withdrawable - credits);
	}

	// 환전 가능 크레딧 = min(잔액, 적립 합계 - 환전 합계). 사용(USE)은 충전분부터 빠진 것으로 본다.
	private int withdrawable(UUID userId, int balance) {
		long earned = creditTransactionRepository.sumAmount(userId, "EARN");
		long withdrawn = -creditTransactionRepository.sumAmount(userId, "WITHDRAW");
		return (int) Math.max(0, Math.min(balance, earned - withdrawn));
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
