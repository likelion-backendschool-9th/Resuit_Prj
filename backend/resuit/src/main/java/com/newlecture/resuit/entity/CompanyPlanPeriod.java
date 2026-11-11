package com.newlecture.resuit.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 기업 Pro 이용 기간. 체험과 결제한 이용권이 한 줄씩 쌓이고, 지금 요금제는 이 줄들로 계산한다.
// 결제는 크레딧 결제(credit_payments)와 같은 흐름(PENDING → PAID/FAILED)을 따르고, PAID가 될 때 이용 기간이 정해진다.
@Entity
@Table(name = "company_plan_periods")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class CompanyPlanPeriod extends BaseCreatedEntity {

    public static final String TRIAL = "TRIAL";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private User company;

    // TRIAL(14일 체험), PRO_1M(30일), PRO_3M(90일)
    @Column(name = "product", nullable = false, length = 20)
    private String product;

    @Column(name = "days", nullable = false)
    private int days;

    // 결제 금액(원). 체험은 0이다.
    @Column(name = "amount_krw", nullable = false)
    private int amountKrw;

    // 토스 결제 키. 승인된 결제만 들어가고, 체험은 비어 있다.
    @Column(name = "pg_transaction_id", length = 200, unique = true)
    private String pgTransactionId;

    // PENDING, PAID, FAILED. 체험은 처음부터 PAID다.
    @Column(name = "status", nullable = false, length = 20)
    private String status;

    // 이용 시작·끝 시각. PAID가 될 때 정해지고, PENDING·FAILED는 비어 있다.
    @Column(name = "starts_at")
    private LocalDateTime startsAt;

    @Column(name = "ends_at")
    private LocalDateTime endsAt;

    public static CompanyPlanPeriod trial(User company, int days, LocalDateTime start) {
        return CompanyPlanPeriod.builder()
                .company(company).product(TRIAL).days(days).amountKrw(0)
                .status("PAID").startsAt(start).endsAt(start.plusDays(days))
                .build();
    }

    public void markPaid(String paymentKey, LocalDateTime start) {
        this.pgTransactionId = paymentKey;
        this.status = "PAID";
        this.startsAt = start;
        this.endsAt = start.plusDays(days);
    }

    public void markFailed() {
        this.status = "FAILED";
    }

    public boolean isPending() {
        return "PENDING".equals(status);
    }

    public boolean isTrial() {
        return TRIAL.equals(product);
    }
}
