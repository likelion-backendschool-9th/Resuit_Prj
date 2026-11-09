package com.newlecture.resuit.entity;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;
import java.time.*;
import java.util.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "credit_payments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class CreditPayment extends BaseCreatedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "amount_krw", nullable = false)
    private int amountKrw;

    @Column(name = "credits", nullable = false)
    private int credits;

    @Column(name = "pg_transaction_id", length = 100)
    private String pgTransactionId;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    // 결제 승인이 끝나면 PAID로 바꾸고, 토스 결제 키(paymentKey)를 결제사 거래 아이디로 남긴다.
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
