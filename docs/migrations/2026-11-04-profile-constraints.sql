-- 기존 PostgreSQL 데이터에 중복이 없는지 확인한 뒤 실행한다.
-- 같은 이름의 제약/인덱스가 이미 있으면 유지한다. 재실행할 수 있다.
BEGIN;

CREATE UNIQUE INDEX IF NOT EXISTS uq_users_username ON users (username);
CREATE UNIQUE INDEX IF NOT EXISTS uq_integrations_user_provider ON integrations (user_id, provider);
CREATE UNIQUE INDEX IF NOT EXISTS uq_pinned_repos_user_order ON pinned_repos (user_id, order_no);
CREATE UNIQUE INDEX IF NOT EXISTS uq_pinned_repos_user_name ON pinned_repos (user_id, repo_full_name);
CREATE UNIQUE INDEX IF NOT EXISTS uq_blog_posts_integration_guid ON blog_posts (integration_id, guid);

CREATE INDEX IF NOT EXISTS ix_posts_profile ON posts(user_id, created_at DESC)
    WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS ix_feedback_comments_profile ON feedback_comments(user_id, created_at DESC);
CREATE INDEX IF NOT EXISTS ix_user_badge_status ON user_badge(user_id, status);

COMMIT;
