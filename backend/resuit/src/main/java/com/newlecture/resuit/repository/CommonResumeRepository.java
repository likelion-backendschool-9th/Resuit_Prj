package com.newlecture.resuit.repository;

import com.newlecture.resuit.entity.CommonResume;
import java.util.Optional;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommonResumeRepository extends JpaRepository<CommonResume, Long> {
    Optional<CommonResume> findByIdAndUserIdAndIsPublicTrue(Long id, UUID userId);
    Optional<CommonResume> findByIdAndUserId(Long id, UUID userId);
    List<CommonResume> findByUserIdOrderByCreatedAtDesc(UUID userId);
}
