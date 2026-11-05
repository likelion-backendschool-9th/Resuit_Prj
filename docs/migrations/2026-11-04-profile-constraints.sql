-- 기존 PostgreSQL 데이터에 중복이 없는지 확인한 뒤 실행한다.
BEGIN;

ALTER TABLE users ADD CONSTRAINT uq_users_username UNIQUE (username);
ALTER TABLE integrations ADD CONSTRAINT uq_integrations_user_provider UNIQUE (user_id, provider);
ALTER TABLE pinned_repos ADD CONSTRAINT uq_pinned_repos_user_order UNIQUE (user_id, order_no);
ALTER TABLE pinned_repos ADD CONSTRAINT uq_pinned_repos_user_name UNIQUE (user_id, repo_full_name);
ALTER TABLE blog_posts ADD CONSTRAINT uq_blog_posts_integration_guid UNIQUE (integration_id, guid);

CREATE INDEX ix_posts_profile ON posts(user_id, created_at DESC)
    WHERE deleted_at IS NULL;
CREATE INDEX ix_feedback_comments_profile ON feedback_comments(user_id, created_at DESC);
CREATE INDEX ix_user_badge_status ON user_badge(user_id, status);

COMMIT;
