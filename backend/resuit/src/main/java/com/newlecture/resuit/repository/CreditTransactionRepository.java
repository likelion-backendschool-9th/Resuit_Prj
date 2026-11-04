package com.newlecture.resuit.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.newlecture.resuit.entity.CreditTransaction;

public interface CreditTransactionRepository extends JpaRepository<CreditTransaction, Long> {

	Optional<CreditTransaction> findFirstByUser_IdOrderByIdDesc(UUID userId);

	Page<CreditTransaction> findByUser_IdOrderByIdDesc(UUID userId, Pageable pageable);
}
