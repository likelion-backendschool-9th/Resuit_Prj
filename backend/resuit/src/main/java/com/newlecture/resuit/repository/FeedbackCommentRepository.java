package com.newlecture.resuit.repository;

import com.newlecture.resuit.entity.FeedbackComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface FeedbackCommentRepository extends JpaRepository<FeedbackComment, Long> {
    @Query("SELECT c.feedbackRequest.id FROM FeedbackComment c WHERE c.commentId = :id")
    Optional<Long> findRequestId(@Param("id") Long id);

    boolean existsByFeedbackRequestIdAndIsAdoptedTrue(Long requestId);
}
