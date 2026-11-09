package com.newlecture.resuit.service.impl;

import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.newlecture.resuit.dto.CreditPaymentConfirmRequest;
import com.newlecture.resuit.dto.CreditPaymentCreateRequest;
import com.newlecture.resuit.dto.CreditPaymentOrderResponse;
import com.newlecture.resuit.dto.CreditPaymentResultResponse;
import com.newlecture.resuit.entity.CreditPayment;
import com.newlecture.resuit.entity.User;
import com.newlecture.resuit.repository.CreditPaymentRepository;
import com.newlecture.resuit.repository.UserRepository;
import com.newlecture.resuit.service.CreditPaymentService;
import com.newlecture.resuit.service.CreditService;
import com.newlecture.resuit.service.TossPaymentsClient;

@Service
public class CreditPaymentServiceImpl implements CreditPaymentService {

    private static final int KRW_PER_CREDIT = 100;
    private static final Set<Integer> PACKAGES = Set.of(100, 300, 500, 1000);
    // 토스에 넘기는 주문번호 앞부분. 뒤에 결제 ID를 붙인다(토스 규칙: 6~64자, 영문·숫자·-·_).
    private static final String ORDER_PREFIX = "resuit-credit-";

    private final CreditPaymentRepository payments;
    private final UserRepository users;
    private final CreditService creditService;
    private final TossPaymentsClient toss;

    public CreditPaymentServiceImpl(CreditPaymentRepository payments, UserRepository users,
            CreditService creditService, TossPaymentsClient toss) {
        this.payments = payments;
        this.users = users;
        this.creditService = creditService;
        this.toss = toss;
    }

    @Override
    @Transactional
    public CreditPaymentOrderResponse create(String username, CreditPaymentCreateRequest request) {
        // 정해진 묶음만 충전할 수 있다. 금액은 서버가 계산하므로 브라우저가 금액을 바꿀 수 없다.
        if (!PACKAGES.contains(request.credits())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "충전할 수 있는 크레딧은 100, 300, 500, 1000입니다.");
        }
        if (!toss.isConfigured()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "결제 설정이 되어 있지 않습니다.");
        }
        User user = users.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        CreditPayment payment = payments.save(CreditPayment.builder()
                .user(user)
                .credits(request.credits())
                .amountKrw(request.credits() * KRW_PER_CREDIT)
                .status("PENDING")
                .build());
        return new CreditPaymentOrderResponse(payment.getId(), ORDER_PREFIX + payment.getId(),
                "Resuit 크레딧 " + payment.getCredits(), payment.getAmountKrw(), payment.getCredits(),
                toss.clientKey(), user.getId().toString());
    }

    // 실패로 바꾼 상태(FAILED)는 예외로 끝나도 저장되어야 하므로 ResponseStatusException에서는 롤백하지 않는다.
    @Override
    @Transactional(noRollbackFor = ResponseStatusException.class)
    public CreditPaymentResultResponse confirm(String username, CreditPaymentConfirmRequest request) {
        CreditPayment payment = payments.findWithLockById(parsePaymentId(request.orderId()))
                .filter(p -> p.getUser().getUsername().equals(username))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "결제를 찾을 수 없습니다."));

        // 이미 승인된 결제를 같은 결제 키로 다시 확인하면(새로고침 등) 다시 충전하지 않고 결과만 돌려준다.
        if ("PAID".equals(payment.getStatus())) {
            if (request.paymentKey().equals(payment.getPgTransactionId())) {
                return toResult(payment, creditService.getBalance(username).balance());
            }
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 처리된 결제입니다.");
        }
        if (!payment.isPending()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 실패한 결제입니다. 다시 충전해 주세요.");
        }
        // 브라우저에서 받은 금액이 주문 금액과 다르면 조작된 요청으로 보고 실패 처리한다.
        if (request.amount() != payment.getAmountKrw()) {
            payment.markFailed();
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "결제 금액이 주문 금액과 다릅니다.");
        }

        TossPaymentsClient.ConfirmResult result = toss.confirm(request.paymentKey(), request.orderId(), payment.getAmountKrw());
        if (!result.approved()) {
            payment.markFailed();
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, result.errorMessage());
        }
        // 승인 응답인데 완료(DONE)가 아니거나 금액이 다르면 결과를 확신할 수 없어 PENDING으로 두고 502로 끝낸다.
        if (!"DONE".equals(result.status()) || result.totalAmount() == null || result.totalAmount() != payment.getAmountKrw()) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "결제 승인 결과를 확인하지 못했습니다.");
        }

        // 결제 완료로 바꾸고, 같은 트랜잭션에서 크레딧 충전 내역(CHARGE)을 남긴다.
        payment.markPaid(request.paymentKey());
        int balance = creditService.charge(payment);
        return toResult(payment, balance);
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

    private CreditPaymentResultResponse toResult(CreditPayment payment, int balance) {
        return new CreditPaymentResultResponse(payment.getId(), ORDER_PREFIX + payment.getId(),
                payment.getCredits(), payment.getAmountKrw(), payment.getStatus(), balance);
    }
}
