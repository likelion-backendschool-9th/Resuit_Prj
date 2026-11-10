-- 태그 없는 기존 게시글은 그대로 유지한다. 별도 backfill은 필요 없다.
CREATE TABLE IF NOT EXISTS post_tags (
    post_id BIGINT NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    tag VARCHAR(50) NOT NULL,
    PRIMARY KEY (post_id, tag)
);
CREATE INDEX IF NOT EXISTS idx_post_tags_tag_post ON post_tags (tag, post_id);
