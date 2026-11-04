package com.newlecture.resuit.repository;

import com.newlecture.resuit.entity.FeedbackComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FeedbackCommentRepository extends JpaRepository<FeedbackComment, Long> {
    @Query("SELECT c.feedbackRequest.id FROM FeedbackComment c WHERE c.commentId = :id")
    Optional<Long> findRequestId(@Param("id") Long id);

    boolean existsByFeedbackRequestIdAndIsAdoptedTrue(Long requestId);

    // 이 회원이 남긴 댓글 수. 삭제된 피드백 요청의 댓글은 뺀다.
    long countByUser_IdAndFeedbackRequest_DeletedAtIsNull(UUID userId);

    // 이 회원의 댓글 중 채택된 수. 삭제된 피드백 요청의 댓글은 뺀다.
    long countByUser_IdAndIsAdoptedTrueAndFeedbackRequest_DeletedAtIsNull(UUID userId);

    // 이 회원의 최근 댓글 5개. 삭제된 요청의 댓글은 빼고, 작성 시각이 같으면 ID 역순으로 정렬한다.
    List<FeedbackComment> findTop5ByUser_IdAndFeedbackRequest_DeletedAtIsNullOrderByCreatedAtDescCommentIdDesc(UUID userId);
}
