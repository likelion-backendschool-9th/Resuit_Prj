package com.newlecture.resuit.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.newlecture.resuit.entity.FeedbackComment;

public interface FeedbackCommentRepository extends JpaRepository<FeedbackComment, Long> {

	long countByUser_IdAndFeedbackRequest_DeletedAtIsNull(UUID userId);

	long countByUser_IdAndIsAdoptedTrueAndFeedbackRequest_DeletedAtIsNull(UUID userId);

	List<FeedbackComment> findTop5ByUser_IdAndFeedbackRequest_DeletedAtIsNullOrderByCreatedAtDescCommentIdDesc(UUID userId);
}
