package com.newlecture.resuit.repository;

import com.newlecture.resuit.entity.Verification;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VerificationRepository extends JpaRepository<Verification, Long> {
    Optional<Verification> findFirstByUserIdAndTypeOrderByIdDesc(UUID userId, String type);

    Optional<Verification> findFirstByUserIdAndTypeAndVerifiedAtIsNotNullOrderByVerifiedAtDesc(
            UUID userId, String type);
}
