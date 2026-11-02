# 채용·이력서·피드백 엔티티

제공된 Resuit ERD를 기준으로 작성한 일반 Java 클래스다. 현재 프로젝트에는 DB 접근 기술이 설정되어 있지 않으므로 JPA 매핑, 테이블 생성, 저장·조회 기능은 포함하지 않는다.

| 테이블 | 클래스 | 기본키 |
| --- | --- | --- |
| job_questions | JobQuestion | UUID id |
| job | Job | UUID id |
| target_resumes | TargetResume | Long id |
| common_resumes | CommonResume | Long id |
| feedback_requests | FeedbackRequest | Long id |
| feedback_comments | FeedbackComment | Long commentId |
| feedback_request_sections | FeedbackRequestSection | Long sectionId |

## 자료형과 컬럼명

- `UUID` → `UUID`, `BIGINT` → `Long`, `INT` → `Integer`.
- `VARCHAR`, `TEXT` → `String`, `DATE` → `LocalDate`, `TIMESTAMP` → `LocalDateTime`.
- Java 필드는 snake_case 컬럼명을 camelCase로 표현한다. DB 연동 시 컬럼 매핑을 설정해야 한다.
- 이력서의 `content`는 `jsonb`에 대응하는 JSON 문자열이다. DB 연동 시 JSON 유효성 검사와 PostgreSQL jsonb 바인딩을 구현해야 한다.
- `CommonResume.Field`의 `FRONTEND`, `BACKEND`는 DB enum의 `frontend`, `backend`에 대응한다. 저장·조회 시 대소문자 변환 매핑이 필요하다.
- ERD에 따라 두 이력서 클래스의 수정일은 `updateAt` (`update_at`), 공고·피드백의 수정일은 `updatedAt` (`updated_at`)으로 구분했다.

## 관계

- `JobQuestion.jobId`, `TargetResume.jobId` → `Job.id`.
- `TargetResume.commonResumeId`, `FeedbackRequest.commonResumeId` → `CommonResume.id`.
- `FeedbackRequest.targetResumeId` → `TargetResume.id`.
- `FeedbackRequestSection.requestId`, `FeedbackComment.feedbackId` → `FeedbackRequest.id`.
- `FeedbackComment.parentId` → `FeedbackComment.commentId` (대댓글).
- `userId`, `Job.companyId` → ERD의 `users.id`. 이번 범위에는 사용자 엔티티를 포함하지 않는다.

관계는 외래키 ID 필드로 표현했다. 기본키 생성 방식, 필수 여부, 기본값, 길이 제한, 삭제 정책, 피드백 요청의 공통/특화 이력서 선택 규칙은 DB 및 서비스 구현 시 확정해야 한다. 현재 클래스는 이 제약을 강제하지 않는다.
