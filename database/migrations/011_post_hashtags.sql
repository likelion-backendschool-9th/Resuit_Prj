-- 모든 게시글 타입에 붙는 자유 해시태그. 기술 태그(post_tech_tags)와 분리한다.
CREATE TABLE IF NOT EXISTS post_hashtags (
    post_id BIGINT NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    tag VARCHAR(50) NOT NULL,
    PRIMARY KEY (post_id, tag)
);
CREATE INDEX IF NOT EXISTS idx_post_hashtags_tag_post ON post_hashtags (tag, post_id);
