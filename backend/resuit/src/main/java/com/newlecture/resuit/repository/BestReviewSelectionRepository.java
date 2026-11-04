package com.newlecture.resuit.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class BestReviewSelectionRepository {
    private final JdbcTemplate jdbc;

    public boolean claim(String period, LocalDate start) {
        return jdbc.update("""
            INSERT INTO best_review_selections (period, period_start)
            VALUES (?, ?) ON CONFLICT (period, period_start) DO NOTHING
            """, period, start) == 1;
    }

    public boolean hasResults(String period, LocalDate start) {
        return Boolean.TRUE.equals(jdbc.queryForObject("""
            SELECT EXISTS (SELECT 1 FROM best_reviews WHERE LOWER(period) = ? AND period_start = ?)
            """, Boolean.class, period, start));
    }

    public record SelectionWindow(String period, LocalDate start, LocalDateTime from, LocalDateTime until) {}

    public int select(SelectionWindow window) {
        return jdbc.update("""
            INSERT INTO best_reviews (comment_id, period, period_start, rank_no, created_at)
            SELECT c.comment_id, ?, ?,
                   ROW_NUMBER() OVER (ORDER BY COUNT(l.user_id) DESC,
                       COALESCE(c.is_adopted AND c.adopted_at < ?, false) DESC,
                       c.created_at, c.comment_id), CURRENT_TIMESTAMP
            FROM feedback_comments c
            LEFT JOIN feedback_comment_likes l ON l.comment_id = c.comment_id AND l.created_at < ?
            WHERE c.created_at >= ? AND c.created_at < ?
            GROUP BY c.comment_id
            ORDER BY COUNT(l.user_id) DESC, COALESCE(c.is_adopted AND c.adopted_at < ?, false) DESC,
                     c.created_at, c.comment_id
            LIMIT 3
            """, window.period(), window.start(), window.until(), window.until(),
                window.from(), window.until(), window.until());
    }
}
