package com.newlecture.resuit.repository;

import com.newlecture.resuit.entity.Verification;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VerificationRepository extends JpaRepository<Verification, Long> {
    Optional<Verification> findFirstByUserIdAndTypeAndVerifiedAtIsNotNullAndRevokedAtIsNullOrderByVerifiedAtDesc(
            UUID userId, String type);

    List<Verification> findByUserIdAndTypeAndVerifiedAtIsNotNullAndRevokedAtIsNull(
            UUID userId, String type);
}
