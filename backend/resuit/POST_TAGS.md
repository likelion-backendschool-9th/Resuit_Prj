# 게시글 기술 태그 · 해시태그

## 기술 태그 (`post_tech_tags`)

- `Post.techTags`: `@ElementCollection Set<String>` → `post_tech_tags(post_id, tag)`.
- 기술 정보(`TECH`) 글에만 붙는다. 허용 목록(React, TypeScript, Spring, Java, Docker, AI) 중 최대 3개.
- API 필드명: `techTags`.
- 빈 목록은 DB 행 없음, 응답에서는 `techTags: null`.

기존 DB의 `post_tags`는 `database/migrations/010_rename_post_tags_to_post_tech_tags.sql`로 이름을 바꾼다.

## 해시태그 (`post_hashtags`)

- `PostHashtag` 엔티티 → `post_hashtags(post_id, tag)`, `Post.hashtags` OneToMany.
- 모든 게시글 타입에 붙일 수 있다. 최대 10개. `#`는 저장하지 않는다.
- 허용: 한글·영문·숫자·`_`·`-` (1~50자).
- API 필드명: `hashtags`.
- 검색: `GET /api/v1/posts/feeds?tab=hashtag&tag={tag}`.

`database/migrations/011_post_hashtags.sql`을 적용한다.
