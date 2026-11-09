package com.newlecture.resuit.service.impl;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.newlecture.resuit.dto.CompanyProPaymentCreateRequest;
import com.newlecture.resuit.dto.CompanyProPaymentOrderResponse;
import com.newlecture.resuit.dto.CompanyProPaymentResultResponse;
import com.newlecture.resuit.dto.CompanySubscriptionResponse;
import com.newlecture.resuit.dto.CreditPaymentConfirmRequest;
import com.newlecture.resuit.entity.CompanyPlanPeriod;
import com.newlecture.resuit.entity.User;
import com.newlecture.resuit.repository.CompanyPlanPeriodRepository;
import com.newlecture.resuit.service.CompanyMemberGuard;
import com.newlecture.resuit.service.CompanyProPaymentService;
import com.newlecture.resuit.service.CompanySubscriptionService;
import com.newlecture.resuit.service.TossPaymentsClient;

// 기업 Pro 이용권 결제. 흐름은 크레딧 충전(CreditPaymentServiceImpl)과 같다.
// 주문(PENDING) → 토스 결제창 → 승인 요청 → 토스 승인 → PAID와 이용 기간 결정(같은 트랜잭션)
// 주문과 이용 기간은 company_plan_periods 한 줄이다.
@Service
public class CompanyProPaymentServiceImpl implements CompanyProPaymentService {

	// 이용권 코드별 일수와 금액. 화면(CompanyProPage)과 같은 값이다.
	private record Product(String name, int days, int amountKrw) {
	}

	private static final Map<String, Product> PRODUCTS = Map.of(
			"PRO_1M", new Product("Resuit 기업 Pro 1개월", 30, 49_000),
			"PRO_3M", new Product("Resuit 기업 Pro 3개월", 90, 129_000));
	// 토스 주문 번호 앞부분. 크레딧 주문(resuit-credit-)과 겹치지 않게 다르게 둔다.
	private static final String ORDER_PREFIX = "resuit-pro-";

	private final CompanyPlanPeriodRepository payments;
	private final CompanyMemberGuard companyGuard;
	private final CompanySubscriptionService subscriptionService;
	private final TossPaymentsClient toss;

	public CompanyProPaymentServiceImpl(CompanyPlanPeriodRepository payments, CompanyMemberGuard companyGuard,
			CompanySubscriptionService subscriptionService, TossPaymentsClient toss) {
		this.payments = payments;
		this.companyGuard = companyGuard;
		this.subscriptionService = subscriptionService;
		this.toss = toss;
	}

	@Override
	@Transactional
	public CompanyProPaymentOrderResponse create(String username, CompanyProPaymentCreateRequest request) {
		User company = companyGuard.requireCompany(username);
		Product product = PRODUCTS.get(request.product());
		if (product == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이용권은 PRO_1M, PRO_3M 중 하나여야 합니다.");
		}
		if (!toss.isConfigured()) {
			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "결제 설정이 되어 있지 않습니다.");
		}
		CompanyPlanPeriod payment = payments.save(CompanyPlanPeriod.builder()
				.company(company)
				.product(request.product())
				.days(product.days())
				.amountKrw(product.amountKrw())
				.status("PENDING")
				.build());
		return new CompanyProPaymentOrderResponse(payment.getId(), ORDER_PREFIX + payment.getId(), product.name(),
				payment.getAmountKrw(), payment.getProduct(), payment.getDays(), toss.clientKey(), company.getId().toString());
	}

	// 결제를 승인하고 Pro 기간을 늘린다. 실패로 바꾼 상태는 예외로 끝나도 저장되어야 하므로 롤백하지 않는다.
	@Override
	@Transactional(noRollbackFor = ResponseStatusException.class)
	public CompanyProPaymentResultResponse confirm(String username, CreditPaymentConfirmRequest request) {
		User company = companyGuard.requireCompany(username);
		CompanyPlanPeriod payment = payments.findWithLockById(parsePaymentId(request.orderId()))
				.filter(p -> p.getCompany().getId().equals(company.getId()))
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "결제를 찾을 수 없습니다."));

		// 체험 줄은 결제가 아니라 승인할 수 없다. 없는 주문처럼 404다.
		if (payment.isTrial()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "결제를 찾을 수 없습니다.");
		}
		// 같은 결제 키로 다시 확인하면(새로고침 등) 기간을 다시 늘리지 않고 결과만 돌려준다.
		if ("PAID".equals(payment.getStatus())) {
			if (request.paymentKey().equals(payment.getPgTransactionId())) {
				return toResult(payment, subscriptionService.getMySubscription(username));
			}
			throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 처리된 결제입니다.");
		}
		if (!payment.isPending()) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 실패한 결제입니다. 다시 결제해 주세요.");
		}
		// 금액이 주문과 다르면 조작된 요청으로 보고 토스를 부르지 않는다.
		if (request.amount() != payment.getAmountKrw()) {
			payment.markFailed();
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "결제 금액이 주문 금액과 다릅니다.");
		}

		TossPaymentsClient.ConfirmResult result = toss.confirm(request.paymentKey(), request.orderId(), payment.getAmountKrw());
		if (!result.approved()) {
			payment.markFailed();
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, result.errorMessage());
		}
		// 완료(DONE)가 아니거나 금액이 다르면 결과를 알 수 없으므로 PENDING으로 두고 502로 끝낸다.
		if (!"DONE".equals(result.status()) || result.totalAmount() == null || result.totalAmount() != payment.getAmountKrw()) {
			throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "결제 승인 결과를 확인하지 못했습니다.");
		}

		// 이용 기간(시작·끝)을 정하고 PAID로 바꾼다. 남은 결제 기간이 있으면 그 뒤로 이어 붙는다.
		return toResult(payment, subscriptionService.activatePaid(company, payment, request.paymentKey()));
	}

	private Long parsePaymentId(String orderId) {
		if (orderId.startsWith(ORDER_PREFIX)) {
			try {
				return Long.parseLong(orderId.substring(ORDER_PREFIX.length()));
			} catch (NumberFormatException ignored) {
			}
		}
		throw new ResponseStatusException(HttpStatus.NOT_FOUND, "결제를 찾을 수 없습니다.");
	}

	private CompanyProPaymentResultResponse toResult(CompanyPlanPeriod payment, CompanySubscriptionResponse subscription) {
		return new CompanyProPaymentResultResponse(payment.getId(), ORDER_PREFIX + payment.getId(), payment.getProduct(),
				payment.getDays(), payment.getAmountKrw(), payment.getStatus(), subscription);
	}
}
