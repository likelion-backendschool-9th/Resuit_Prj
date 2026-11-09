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
    boolean existsByComment_CommentId(Long commentId);

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

    interface ReviewerRow {
        long getRank();
        String getUsername();
        String getNickname();
        long getReviewCount();
        long getSelectionCount();
        long getWeeklyCount();
        long getMonthlyCount();
        Long getRepresentativeReviewId();
        String getRepresentativeContent();
        LocalDate getLatestSelection();
    }

    @Query(value = """
        WITH stats AS (
            SELECT c.user_id,
                   COUNT(DISTINCT b.comment_id) AS review_count,
                   COUNT(*) AS selection_count,
                   COUNT(*) FILTER (WHERE LOWER(b.period) = 'weekly') AS weekly_count,
                   COUNT(*) FILTER (WHERE LOWER(b.period) = 'monthly') AS monthly_count,
                   MAX(b.period_start) AS latest_selection
            FROM best_reviews b JOIN feedback_comments c ON c.comment_id = b.comment_id
            GROUP BY c.user_id
        )
        SELECT ROW_NUMBER() OVER (ORDER BY s.review_count DESC, s.selection_count DESC, u.username ASC) AS rank,
               u.username, u.nickname, s.review_count AS "reviewCount", s.selection_count AS "selectionCount",
               s.weekly_count AS "weeklyCount", s.monthly_count AS "monthlyCount",
               s.latest_selection AS "latestSelection",
               representative.comment_id AS "representativeReviewId",
               representative.content AS "representativeContent"
        FROM stats s JOIN users u ON u.id = s.user_id
        JOIN LATERAL (
            SELECT c.comment_id, c.content FROM best_reviews b
            JOIN feedback_comments c ON c.comment_id = b.comment_id
            WHERE c.user_id = s.user_id
            ORDER BY b.rank_no ASC, b.period_start DESC, b.id DESC LIMIT 1
        ) representative ON true
        ORDER BY s.review_count DESC, s.selection_count DESC, u.username ASC
        """, countQuery = """
        SELECT COUNT(DISTINCT c.user_id)
        FROM best_reviews b JOIN feedback_comments c ON c.comment_id = b.comment_id
        JOIN users u ON u.id = c.user_id
        """, nativeQuery = true)
    Page<ReviewerRow> findHallOfFameReviewers(Pageable pageable);

    @Query("""
        select count(b) from BestReview b
        where b.comment.user.id = :userId
          and b.periodStart >= :from
          and b.periodStart < :to
        """)
    long countForReviewer(@Param("userId") UUID userId,
                          @Param("from") LocalDate from,
                          @Param("to") LocalDate to);
}
