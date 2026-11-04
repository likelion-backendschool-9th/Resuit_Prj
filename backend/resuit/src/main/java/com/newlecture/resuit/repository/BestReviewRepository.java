package com.newlecture.resuit.repository;

import com.newlecture.resuit.entity.BestReview;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BestReviewRepository extends JpaRepository<BestReview, Long> {
    interface HistoryRow extends ReviewRow {
        Long getSelectionId();
        String getPeriod();
    }

    interface ReviewRow {
        Long getReviewId();
        String getUsername();
        String getNickname();
        String getContent();
        String getSection();
        boolean getAdopted();
        LocalDate getPeriodStart();
        int getRank();
        long getLikeCount();
        boolean getLiked();
        boolean getCanAccept();
    }

    @Query(value = """
        SELECT c.comment_id AS "reviewId", u.username, u.nickname, c.content, c.section,
               c.is_adopted AS adopted, b.period_start AS "periodStart", b.rank_no AS rank,
               (SELECT COUNT(*) FROM feedback_comment_likes l WHERE l.comment_id = c.comment_id) AS "likeCount",
               EXISTS (SELECT 1 FROM feedback_comment_likes l
                       WHERE l.comment_id = c.comment_id AND l.user_id = :viewerId) AS liked,
               (r.user_id = :viewerId AND r.deleted_at IS NULL AND NOT c.is_adopted
                AND NOT EXISTS (SELECT 1 FROM feedback_comments accepted
                                WHERE accepted.feedback_id = r.id AND accepted.is_adopted)) AS "canAccept"
        FROM best_reviews b
        JOIN feedback_comments c ON c.comment_id = b.comment_id
        JOIN feedback_requests r ON r.id = c.feedback_id
        JOIN users u ON u.id = c.user_id
        WHERE LOWER(b.period) = :period
          AND b.period_start = (SELECT MAX(x.period_start) FROM best_reviews x WHERE LOWER(x.period) = :period)
        ORDER BY b.rank_no, b.id
        """, nativeQuery = true)
    List<ReviewRow> findLatest(@Param("period") String period, @Param("viewerId") UUID viewerId);

    @Query(value = """
        SELECT b.id AS "selectionId", UPPER(b.period) AS period,
               c.comment_id AS "reviewId", u.username, u.nickname, c.content, c.section,
               c.is_adopted AS adopted, b.period_start AS "periodStart", b.rank_no AS rank,
               (SELECT COUNT(*) FROM feedback_comment_likes l WHERE l.comment_id = c.comment_id) AS "likeCount",
               EXISTS (SELECT 1 FROM feedback_comment_likes l
                       WHERE l.comment_id = c.comment_id AND l.user_id = :viewerId) AS liked,
               (r.user_id = :viewerId AND r.deleted_at IS NULL AND NOT c.is_adopted
                AND NOT EXISTS (SELECT 1 FROM feedback_comments accepted
                                WHERE accepted.feedback_id = r.id AND accepted.is_adopted)) AS "canAccept"
        FROM best_reviews b
        JOIN feedback_comments c ON c.comment_id = b.comment_id
        JOIN feedback_requests r ON r.id = c.feedback_id
        JOIN users u ON u.id = c.user_id
        ORDER BY b.period_start DESC, b.period, b.rank_no, b.id
        """, countQuery = """
        SELECT COUNT(*) FROM best_reviews b
        JOIN feedback_comments c ON c.comment_id = b.comment_id
        JOIN feedback_requests r ON r.id = c.feedback_id
        JOIN users u ON u.id = c.user_id
        """, nativeQuery = true)
    Page<HistoryRow> findHistory(@Param("viewerId") UUID viewerId, Pageable pageable);
}
