-- 수동 적용: 먼저 대상 DB와 기존 중복 채택을 확인한다. 중복이 있으면 실패하고 데이터는 보존한다.
BEGIN;
CREATE UNIQUE INDEX IF NOT EXISTS uq_feedback_comments_one_accepted
    ON feedback_comments (feedback_id) WHERE is_adopted = true;
COMMIT;
