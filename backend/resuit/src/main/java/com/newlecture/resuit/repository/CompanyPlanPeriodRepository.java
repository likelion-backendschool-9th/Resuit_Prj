package com.newlecture.resuit.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.newlecture.resuit.entity.CompanyPlanPeriod;

import jakarta.persistence.LockModeType;

public interface CompanyPlanPeriodRepository extends JpaRepository<CompanyPlanPeriod, Long> {

    // 같은 결제를 동시에 두 번 승인하려 해도 한 번만 처리되도록 결제 행을 잠그고 읽는다. 기업 회원도 함께 읽는다.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM CompanyPlanPeriod p JOIN FETCH p.company WHERE p.id = :id")
    Optional<CompanyPlanPeriod> findWithLockById(@Param("id") Long id);

    // 기업의 이용 기간이 있는 줄(PAID: 체험과 승인된 결제)을 모두 읽는다. 회사마다 몇 줄뿐이라 계산은 서비스에서 한다.
    List<CompanyPlanPeriod> findByCompany_IdAndStatus(UUID companyId, String status);
}
