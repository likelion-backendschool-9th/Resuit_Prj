package com.newlecture.resuit.repository;

import com.newlecture.resuit.entity.FeedbackComment;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileFeedbackRepository extends JpaRepository<FeedbackComment, Long> {
    Page<FeedbackComment> findByUserId(UUID userId, Pageable pageable);
    long countByUserId(UUID userId);
}
