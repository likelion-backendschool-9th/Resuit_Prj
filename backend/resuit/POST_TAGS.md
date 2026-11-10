# 게시글 해시태그

- `Post.tags`: `@ElementCollection Set<String>` → `post_tags(post_id, tag)`.
- 기술 태그 외 용도로 확장할 수 있도록 필드와 테이블은 일반 이름을 사용한다. 현재 서비스 정책은 TECH 글에 React, TypeScript, Spring, Java, Docker, AI 중 최대 3개만 허용한다.
- 작성·수정 요청의 `tags`는 생략/null/빈 배열을 허용한다. 모두 태그 없음으로 처리한다. 수정 시 전달한 목록으로 교체하므로 생략/null도 기존 태그를 제거한다.
- 일반·질문 글은 태그를 저장하지 않는다. TECH에서 다른 종류로 수정하면 기존 태그가 제거된다.
- JPA 컬렉션은 빈 Set으로 유지한다. DB에서 태그 없음은 post_tags 행이 없는 상태이며, 게시글 목록·상세 응답은 `tags: null`을 반환한다. tag 컬럼 자체는 NOT NULL로, 의미 없는 null 행은 만들지 않는다.
- `(post_id, tag)` 중복 방지 및 `(tag, post_id)` 검색 인덱스를 둔다. 컬렉션은 LAZY + BatchSize(100)로 조회한다.

## API

기존 POST /api/v1/posts, PATCH /api/v1/posts/{id}에 `"tags": ["React", "TypeScript"]`를 추가한다. 목록·상세 응답에도 tags를 포함한다.

기존 GET /api/v1/posts/feeds?tab=tech에 선택 쿼리 `tag=React`를 추가한다. 전체는 tag를 생략한다. DB에서 태그 조건을 적용한 뒤 페이지네이션한다. 별도 태그 조회 API는 만들지 않는다. 태그는 게시글의 작성 및 표시 데이터이고, 선택지는 현재 고정되어 있기 때문이다.

## 기존 DB 적용

`database/migrations/008_post_tags.sql`을 적용한다. 기존 게시글은 이관할 태그가 없으므로 backfill이 필요 없다. 로컬 마이그레이션 스크립트는 이 파일을 자동 포함한다. 이 기능 구현 과정에서는 실제 DB에 SQL을 실행하지 않았다.
