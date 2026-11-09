# 최신 코드 병합 후 DB 확인 안내

기준일: 2026-11-05. Git pull/merge는 코드와 SQL 파일을 가져오며, 각자의 PostgreSQL 스키마와 데이터는 변경하지 않는다. 아래 변경은 모두 이번 UI 작업에서 새로 생긴 요구가 아니다. 기존 dev에 반영된 공고 ID·배지 기능과 리뷰·인증 기능의 DB 준비 상태도 함께 확인해야 한다.

## 먼저 구분할 사항

- 명예의 전당의 리뷰어 순위는 기존 `best_reviews`, `feedback_comments`, `users`를 집계한다. 이 기능 때문에 새 테이블이나 컬럼을 추가하지 않는다. API 응답이 `reviews`에서 `reviewers`로 바뀌므로 백엔드와 해당 프론트를 함께 반영한다.
- 세션 조회 서비스 분리, 리뷰 요약 카드, 명예의 전당 UI, 공개 프로필의 백엔드 주소 수정은 스키마 변경이 없다.
- 이전 DB를 사용하는 경우 아래 공고 ID, 배지 구조, 리뷰·인증 제약의 누락 때문에 최신 백엔드가 실행되지 않거나 기능 호출이 실패할 수 있다.

## 1. 대상 DB를 먼저 읽기 전용으로 확인

아래 SQL은 조회만 한다. 연결한 DB가 개인·개발·공용 중 어디인지 먼저 확인한다.

```sql
SELECT current_database(), current_user;

SELECT table_name, column_name, data_type, is_identity, identity_generation
FROM information_schema.columns
WHERE table_schema = 'public'
  AND (
    (table_name IN ('job', 'job_questions') AND column_name = 'id')
    OR (table_name IN ('job_questions', 'job_applications',
                      'target_resumes', 'company_offers') AND column_name = 'job_id')
    OR (table_name = 'user_badge' AND column_name IN ('status', 'grace_until'))
    OR (table_name = 'verifications' AND column_name = 'failed_attempts')
  )
ORDER BY table_name, column_name;

SELECT table_name
FROM information_schema.tables
WHERE table_schema = 'public'
  AND table_name IN ('user_badge_history', 'social_accounts', 'best_review_selections');

SELECT tablename, indexname, indexdef
FROM pg_indexes
WHERE schemaname = 'public'
  AND tablename IN ('feedback_comments', 'best_reviews', 'social_accounts', 'badge');
```

실제 DB의 물리 테이블명을 확인한다. 과거 ERD의 `jobs`와 현재 엔티티의 `job`이 다를 수 있다.

## 2. 공고 PK·FK 자료형

현재 엔티티와 API의 기준은 다음과 같다.

| 대상 | 필요한 자료형 |
| --- | --- |
| `job.id`, `job_questions.id` | BIGINT, PK, IDENTITY 자동 생성 |
| `job_questions.job_id` | BIGINT, `job.id` 참조 |
| `job_applications.job_id` | BIGINT, `job.id` 참조 |
| `target_resumes.job_id` | BIGINT, `job.id` 참조 |
| `company_offers.job_id` | BIGINT, `job.id` 참조 |

이미 위 상태라면 공고 ID 이관은 필요 없다. UUID 상태라면 `ddl-auto=update`에 의존해 바꾸지 않는다. UUID 문자열을 숫자로 직접 캐스팅하는 것으로 해결되지 않는다.

- **기존 데이터가 있으면:** 전체 백업을 만들고 복원 가능 여부를 확인한다. 별도 복원 DB에서 기존 UUID → 새 BIGINT 대응표를 만들고, 공고 PK와 참조 FK를 함께 이관한다. `job_questions.id`도 따로 변환한다. 기존 제약·인덱스·기본값을 확인하고, 고아 참조가 없는지 및 저장 후 재조회 결과를 검증한 뒤 해당 DB에 맞는 이관 SQL을 확정한다. 이 문서는 데이터가 있는 DB의 실행용 이관 SQL이 아니다.
- **관련 테이블이 모두 비어 있으면:** 변환할 ID는 없지만 FK를 보존하면서 자료형과 IDENTITY를 함께 변경해야 한다. 다른 구조의 DB에 개인 DB용 SQL을 그대로 실행하지 않는다.
- **새 DB라면:** 최신 엔티티에 맞춰 초기화하고 별도 SQL로 관리하는 제약도 적용한다. 기존 DB에 `create`/`create-drop`을 적용하는 방법과 구분한다.

## 3. 배지 구조

기존 dev의 배지 기능에는 `user_badge.status`, `user_badge.grace_until`, `user_badge_history`가 필요하다.

- 변경 SQL: [배지 갱신 마이그레이션](../migrations/2026-11-04-reviewer-badge-renewal.sql).
- 프로필 제약: [프로필 제약 마이그레이션](../migrations/2026-11-04-profile-constraints.sql).
- 프로필 SQL은 `user_badge.status`를 참조하므로 배지 구조 준비 후 검토한다.
- 두 파일은 자동 실행되지 않으며, 이미 적용한 DB에 그대로 재실행하는 용도가 아니다. 기존 컬럼·중복 값·동등한 제약을 먼저 확인하고 필요한 부분만 해당 환경에 맞게 적용한다. 예를 들어 username 유니크 제약은 이미 리뷰·로그인 SQL로 준비돼 있을 수 있다.

## 4. 리뷰·인증 SQL

아래 파일은 기존 커밋에 포함돼 있지만, 파일을 받았다는 사실이 DB 적용을 의미하지 않는다.

| 파일 | 반영 내용 |
| --- | --- |
| [001](../../database/migrations/001_one_accepted_review.sql) | 요청당 채택 리뷰 최대 1개인 부분 유니크 인덱스 |
| [002](../../database/migrations/002_verification_attempts.sql) | 인증 코드 입력 실패 횟수 |
| [003](../../database/migrations/003_best_review_selection.sql) | 기간별 선정 실행 기록, 기간 내 순위·댓글 중복 방지 |
| [004](../../database/migrations/004_social_accounts.sql) | 소셜 계정 연결, provider + subject 유니크, username 유니크 |

기존 테이블과 데이터가 준비된 DB를 전제로 한다. 기존 중복·컬럼 차이가 있으면 먼저 조사한다. 개인 로컬 스크립트 `scripts/migrate-local-db.sh`는 001~004와 기업 이메일 도메인 초기 데이터 파일을 실행하며 공고 ID 이관이나 배지 변경은 수행하지 않는다. 공용 DB에 개인 실행 스크립트를 사용하지 않는다.

## 5. 적용과 검증

1. DB 연결 대상을 확인하고 전체 백업을 만든다. 백업에는 개인정보가 포함될 수 있으므로 Git에 커밋하지 않는다.
2. 별도 DB에 복원해 필요한 변경을 먼저 적용한다. FK 관계·기존 행 수·중복 제약과 ID 생성 상태를 검증한다.
3. 최신 백엔드를 `SPRING_JPA_HIBERNATE_DDL_AUTO=validate`로 시작해 엔티티와 DB 자료형을 확인한다. 이 검증만으로 모든 인덱스·업무 제약이 보장되는 것은 아니므로 1절의 조회 결과도 대조한다.
4. 변경된 기능의 정상·실패 API와 실제 저장·재조회를 확인한다. 공고·문항·지원·특화 이력서·제안의 관계를 사용하는 기능을 함께 확인한다.
5. 검증한 SQL을 대상 DB에 적용하고 결과를 기록한다. 공용 DB는 담당자와 적용 시점을 맞추고 작업 중 쓰기 요청의 처리도 정한다.

`.env`의 `DDL_AUTO=update`는 Compose 백엔드 재생성 시 자동 DDL을 시도하는 설정이다. UUID 관계의 데이터 이관이나 부분 유니크 인덱스 적용을 대신하지 않는다. 로컬 `scripts/dev-backend.sh`는 별도로 `validate`를 지정한다. 실제 DB와 코드가 이미 맞으면 불필요한 이관을 하지 않는다.

## 개인 환경에서 확인한 범위

개인 DB는 공고 관련 다섯 테이블이 비어 있어 PK·FK 자료형을 함께 변경했고, 배지 변경도 반영했다. 별도 복원 DB 및 개인 DB에서 최신 엔티티 검증·기동이 성공했다. 당시 회원 23명, 리뷰 54개, 선정 기록 54개를 보존했다. 이는 팀 DB의 상태나 이관 성공을 보장하는 결과가 아니다. 개인 백업과 개인 DB 전용 이관 SQL은 이 안내 커밋에 포함하지 않는다.


## 최종 변경 검증 기록 — 2026-11-05

- 백엔드 Java 컴파일 및 프론트 프로덕션 빌드 통과.
- 개인 PostgreSQL을 사용하는 API에서 명예의 전당 순위·페이지 이동·빈 페이지와 잘못된 입력의 400 응답 확인.
- 실제 브라우저에서 작성자 프로필 이동·대표 리뷰 펼치기·모바일 가로 넘침 여부 확인.
- 사이드바의 명예의 전당 API에만 503 응답 대역을 적용했을 때에도 베스트 리뷰 카드 3건이 표시됨을 확인. 요약은 상위 3명만 요청하도록 조정.
- 최종 브라우저 검증에서 수집한 pageerror 0건. 외부 OAuth 로그인·SMTP 발송·전체 기능 E2E 완료를 의미하지 않는다.
- 검토 중 미사용 이력 쿼리·색상 토큰·사이드바 분기를 정리하고, 프로필은 실제 404만 회원 없음으로 처리하도록 수정했다. 기능별 보완은 해당 기능 커밋에 포함했다.
