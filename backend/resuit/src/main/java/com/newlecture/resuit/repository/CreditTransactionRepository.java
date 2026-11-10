package com.newlecture.resuit.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.newlecture.resuit.entity.CreditTransaction;

public interface CreditTransactionRepository extends JpaRepository<CreditTransaction, Long> {

	Optional<CreditTransaction> findFirstByUser_IdOrderByIdDesc(UUID userId);

	Page<CreditTransaction> findByUser_IdOrderByIdDesc(UUID userId, Pageable pageable);

	// 회원의 한 종류 내역 합계. 환전 가능 크레딧(적립 합계 - 환전 합계)을 구할 때 쓴다.
	@Query("SELECT COALESCE(SUM(t.amount), 0) FROM CreditTransaction t WHERE t.user.id = :userId AND t.type = :type")
	long sumAmount(@Param("userId") UUID userId, @Param("type") String type);
}
