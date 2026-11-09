package com.newlecture.resuit.entity;

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

// 기업 Pro 이용권 결제. 크레딧 결제(credit_payments)와 같은 흐름(PENDING → PAID/FAILED)을 따른다.
@Entity
@Table(name = "company_pro_payments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class CompanyProPayment extends BaseCreatedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private User company;

    // 이용권 코드: PRO_1M(30일), PRO_3M(90일)
    @Column(name = "product", nullable = false, length = 20)
    private String product;

    @Column(name = "days", nullable = false)
    private int days;

    @Column(name = "amount_krw", nullable = false)
    private int amountKrw;

    @Column(name = "pg_transaction_id", length = 200, unique = true)
    private String pgTransactionId;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    public void markPaid(String paymentKey) {
        this.pgTransactionId = paymentKey;
        this.status = "PAID";
    }

    public void markFailed() {
        this.status = "FAILED";
    }

    public boolean isPending() {
        return "PENDING".equals(status);
    }
}
