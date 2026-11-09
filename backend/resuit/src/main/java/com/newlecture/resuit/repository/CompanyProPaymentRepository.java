package com.newlecture.resuit.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.newlecture.resuit.entity.CompanyProPayment;

import jakarta.persistence.LockModeType;

public interface CompanyProPaymentRepository extends JpaRepository<CompanyProPayment, Long> {

    // 같은 결제를 동시에 두 번 승인하려 해도 한 번만 처리되도록 결제 행을 잠그고 읽는다. 기업 회원도 함께 읽는다.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM CompanyProPayment p JOIN FETCH p.company WHERE p.id = :id")
    Optional<CompanyProPayment> findWithLockById(@Param("id") Long id);
}
