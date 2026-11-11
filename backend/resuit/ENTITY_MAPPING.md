# 채용·이력서·피드백 JPA 엔티티

제공된 Resuit ERD를 기준으로 7개 클래스를 JPA 엔티티로 매핑했다. Spring Data JPA와 PostgreSQL JDBC 드라이버를 사용한다.

| 테이블 | 클래스 | 기본키 생성 |
| --- | --- | --- |
| job_questions | JobQuestion | Long / IDENTITY |
| job | Job | Long / IDENTITY |
| target_resumes | TargetResume | Long / IDENTITY |
| common_resumes | CommonResume | Long / IDENTITY |
| feedback_requests | FeedbackRequest | Long / IDENTITY |
| feedback_comments | FeedbackComment | Long commentId / IDENTITY |
| feedback_request_sections | FeedbackRequestSection | Long sectionId / IDENTITY |

## 작성 방식

- `@Entity`, `@Table`, `@Column`으로 테이블명과 컬럼명, 문자열 길이를 명시한다.
- `@Getter`와 protected 기본 생성자를 사용한다. `@Data`를 제거하여 연관 객체에 대한 자동 equals/hashCode/toString 호출을 피한다.
- 새 객체는 `Job.builder().title("공고 제목").build()`처럼 builder로 생성한다. 생성할 때 기본키·생성일·수정일을 지정하지 않는다.
- `UUID`, `BIGINT`, `INT`는 각각 `UUID`, `Long`, `Integer`에 대응한다.
- `DATE`는 `LocalDate`, 시간대 없는 `TIMESTAMP`는 `LocalDateTime`으로 표현한다. 생성·수정 시각은 애플리케이션 서버의 시간대를 따른다.
- `@PrePersist`에서 생성·수정 시각을 설정하고 `@PreUpdate`에서 수정 시각을 갱신한다. JPQL bulk update 및 직접 SQL은 이 콜백을 거치지 않는다.
- ERD에 따라 두 이력서의 수정일 컬럼은 `update_at`, 공고·피드백은 `updated_at`이다.

## 관계

아래 관계는 모두 단방향 `@ManyToOne(fetch = FetchType.LAZY)`와 `@JoinColumn`으로 매핑했다.

| 엔티티 필드 | 외래키 컬럼 | 참조 |
| --- | --- | --- |
| JobQuestion.job | job_id | Job.id |
| TargetResume.job | job_id | Job.id |
| TargetResume.commonResume | common_resume_id | CommonResume.id |
| FeedbackRequest.commonResume | common_resume_id | CommonResume.id |
| FeedbackRequest.targetResume | target_resume_id | TargetResume.id |
| FeedbackRequestSection.request | request_id | FeedbackRequest.id |
| FeedbackComment.feedback | feedback_id | FeedbackRequest.id |
| FeedbackComment.parent | parent_id | FeedbackComment.commentId |

예: `JobQuestion.builder().job(job).content("지원 동기는?").build()`. 관계 필드는 기존의 `jobId` 같은 ID 대신 엔티티 객체를 받는다. cascade는 설정하지 않았으므로 참조하는 새 엔티티는 먼저 저장해야 한다. 부모 댓글 삭제 시 자식 댓글을 자동 삭제하지 않는다.

현재 브랜치에는 `User` 엔티티가 없으므로 `userId`와 `Job.companyId`는 UUID 컬럼으로 유지했다. 사용자 엔티티가 합쳐지면 `User`와의 관계 및 외래키 매핑을 추가해야 한다. 현재 Hibernate 생성 DDL에는 사용자 테이블 외래키가 포함되지 않는다.

## JSONB와 직무 enum

- 이력서 `content`는 JSON 문서 문자열이다. `@JdbcTypeCode(SqlTypes.JSON)`과 `columnDefinition = "jsonb"`로 PostgreSQL JSONB에 매핑한다. 예: `"{\"skills\":[\"Java\"]}"`.
- `CommonResume.Field.FRONTEND`, `BACKEND`는 `@EnumeratedValue`를 통해 DB의 소문자 `frontend`, `backend` 값에 대응한다.
- `@JdbcTypeCode(SqlTypes.NAMED_ENUM)`으로 PostgreSQL enum을 사용한다. ERD에는 SQL enum 타입명이 없으므로 Hibernate 기본 이름인 `Field`(PostgreSQL에서 `field`)를 사용한다. 기존 DB의 타입명이 다르면 실제 스키마에 맞춰 조정해야 한다.
- JSON과 enum 매핑은 [Hibernate 공식 문서](https://docs.hibernate.org/orm/7.2/userguide/html_single/)를 참고했다.

## DB 연결과 남은 범위

실행할 때 다음 환경 변수를 실제 개발 DB에 맞게 설정해야 한다. 비밀번호는 저장소에 커밋하지 않는다.

```text
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/resuit
SPRING_DATASOURCE_USERNAME=<DB 사용자>
SPRING_DATASOURCE_PASSWORD=<DB 비밀번호>
SPRING_JPA_HIBERNATE_DDL_AUTO=validate
```

`validate`는 테이블을 생성하지 않는다. 실제 스키마는 별도 마이그레이션으로 준비해야 한다. 새 임시 로컬 DB에서만 `create-drop`으로 매핑을 확인할 수 있다. JPA 의존성 추가 이후 DB 설정 없이 애플리케이션이나 기존 `@SpringBootTest`를 실행하면 DataSource 초기화가 실패할 수 있다.

기본키 자동 생성과 생성·수정일 필수 처리는 이번 구현에서 선택한 정책이다. ERD 이미지에는 컬럼별 NULL 허용 여부가 명확하지 않아 나머지 컬럼의 필수 제약을 임의로 추가하지 않았다. 피드백 요청의 공통/특화 이력서 선택 규칙, 부모 댓글과 자식 댓글이 같은 요청에 속하는지 검사, 삭제 정책, 서비스·리포지토리·API는 별도 구현 범위다.


## 공고·문항 기본키 타입 변경

Job.id와 JobQuestion.id는 PostgreSQL BIGINT에 대응하는 Long과 GenerationType.IDENTITY를 사용한다. 참조 엔티티는 Job 연관관계로 매핑되어 있으므로 FK Java 필드를 따로 수정하지 않는다. resuit_erd_import.sql의 해당 PK·job_id는 이미 BIGINT였으며 두 PK에 IDENTITY 생성을 명시했다. 이 문서 SQL의 공고 테이블명은 jobs, 현재 엔티티는 job으로 서로 다르므로 문서 전체를 기존 DB에 그대로 실행하지 않는다.

이미 UUID로 생성된 실제 DB의 컬럼은 이번 코드 변경으로 변환되지 않는다. 기존 데이터를 유지하려면 UUID→새 BIGINT 매핑을 만들고 job_questions·job_applications·target_resumes·company_offers의 job_id도 함께 이관해야 한다. 이번 작업에서는 실제 DB 변경을 실행하지 않았다.
