BEGIN;
CREATE TABLE IF NOT EXISTS best_review_selections (
    period VARCHAR(20) NOT NULL,
    period_start DATE NOT NULL,
    completed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (period, period_start)
);
CREATE UNIQUE INDEX IF NOT EXISTS uq_best_reviews_period_rank
    ON best_reviews (LOWER(period), period_start, rank_no);
CREATE UNIQUE INDEX IF NOT EXISTS uq_best_reviews_period_comment
    ON best_reviews (LOWER(period), period_start, comment_id);
COMMIT;
