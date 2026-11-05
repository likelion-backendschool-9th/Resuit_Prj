package com.newlecture.resuit.repository;

import com.newlecture.resuit.entity.PinnedRepo;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PinnedRepoRepository extends JpaRepository<PinnedRepo, Long> {
    List<PinnedRepo> findByUserIdOrderByOrderNoAsc(UUID userId);
    void deleteByUserId(UUID userId);
}
