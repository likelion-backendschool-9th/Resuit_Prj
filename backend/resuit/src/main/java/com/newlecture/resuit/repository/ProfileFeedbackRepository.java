package com.newlecture.resuit.repository;

import com.newlecture.resuit.entity.FeedbackComment;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileFeedbackRepository extends JpaRepository<FeedbackComment, Long> {
    @EntityGraph(attributePaths = {
        "feedbackRequest",
        "feedbackRequest.commonResume",
        "feedbackRequest.targetResume"
    })
    Page<FeedbackComment> findByUserId(UUID userId, Pageable pageable);
    long countByUserId(UUID userId);
}
