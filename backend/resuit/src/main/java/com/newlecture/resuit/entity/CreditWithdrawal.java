package com.newlecture.resuit.entity;

import jakarta.persistence.*;
import lombok.*;

// 적립 크레딧을 현금으로 바꾼 기록이다. 관리자 승인 없이 신청하면 바로 완료된다.
@Entity
@Table(name = "credit_withdrawals")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class CreditWithdrawal extends BaseCreatedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "credits", nullable = false)
    private int credits;

    // 받는 금액(원). 수수료를 뺀 값이다.
    @Column(name = "amount_krw", nullable = false)
    private int amountKrw;

    // 수수료(원). 수수료율이 바뀌어도 당시 값이 남도록 저장한다.
    @Column(name = "fee_krw", nullable = false)
    private int feeKrw;

    @Column(name = "bank_name", nullable = false, length = 30)
    private String bankName;

    @Column(name = "account_number", nullable = false, length = 30)
    private String accountNumber;

    @Column(name = "account_holder", nullable = false, length = 50)
    private String accountHolder;
}
