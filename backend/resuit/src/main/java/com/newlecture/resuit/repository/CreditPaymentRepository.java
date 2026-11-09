package com.newlecture.resuit.repository;

import com.newlecture.resuit.entity.CreditPayment;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CreditPaymentRepository extends JpaRepository<CreditPayment, Long> {

    // 같은 결제를 동시에 두 번 승인하려 해도 하나씩 처리되므로, 크레딧은 한 번만 충전된다(설계 문서 기준).
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM CreditPayment p JOIN FETCH p.user WHERE p.id = :id")
    Optional<CreditPayment> findWithLockById(@Param("id") Long id);
}
