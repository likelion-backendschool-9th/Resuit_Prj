package com.newlecture.resuit.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.newlecture.resuit.entity.CreditWithdrawal;

public interface CreditWithdrawalRepository extends JpaRepository<CreditWithdrawal, Long> {
}
