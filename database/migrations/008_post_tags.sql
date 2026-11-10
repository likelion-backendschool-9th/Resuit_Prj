-- 기술 정보 탭 전용 태그. 태그 없는 기존 게시글은 그대로 유지한다.
-- 테이블명은 post_tech_tags. 예전에 post_tags로 만든 DB는 010 마이그레이션이 이름을 바꾼다.
CREATE TABLE IF NOT EXISTS post_tech_tags (
    post_id BIGINT NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    tag VARCHAR(50) NOT NULL,
    PRIMARY KEY (post_id, tag)
);
CREATE INDEX IF NOT EXISTS idx_post_tech_tags_tag_post ON post_tech_tags (tag, post_id);
