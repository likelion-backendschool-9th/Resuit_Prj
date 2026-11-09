# 채용 지원 API

모든 지원·조회·취소 요청은 로그인 세션을 사용한다. userId/username을 클라이언트에서 받지 않는다.
쓰기 요청은 기존 `/api/v1/auth/session`이 제공하는 CSRF 토큰과 세션 쿠키를 사용한다.

## 본인 현재 지원 조회

`GET /api/v1/jobs/{jobId}/applications/me`

현재 지원은 `(jobId, 로그인 사용자)`에 대해 `canceledAt IS NULL`인 기록이다.
취소 기록만 있거나 기록이 없으면 HTTP 200으로 다음 응답을 반환한다.

```json
{"applied":false,"jobId":1,"id":null,"resumeType":null,"resumeId":null,"status":null,"appliedAt":null}
```

지원 중인 경우 예시:

```json
{"applied":true,"jobId":1,"id":10,"resumeType":"TARGET","resumeId":20,"status":"APPLIED","appliedAt":"2026-11-09T12:00:00"}
```

- `resumeType`: `COMMON` 또는 `TARGET`. 공고의 현재 유형이 아니라 실제 제출 이력서 관계로 결정한다.
- `id`: 지원 내역 ID. `resumeId`: 해당 유형의 제출 이력서 ID.
- `status`: 저장된 지원 상태. 제출 당시 이력서 내용의 스냅샷을 반환하는 API는 아니다.
- 공개된 OPEN/CLOSED 공고를 대상으로 하며 비공개 초안·삭제·존재하지 않는 공고는 404이다.
- 비로그인은 401이다. 공개 공고 GET 허용 규칙보다 먼저 본인 조회 경로를 인증 대상으로 지정한다.
- `Cache-Control: no-store`. 공개 상세와 개인 상태의 접근 권한·캐시 정책이 달라 전용 API로 분리한다.

## 지원 및 결과

| API | 요청 | 응답 DTO | 이력서 필드 |
| --- | --- | --- | --- |
| `POST /api/v1/jobs/{jobId}/applications` | `{"commonResumeId":20}` | JobApplicationCommonResponse | commonResumeId |
| `POST /api/v1/jobs/{jobId}/special-applications` | `{"targetResumeId":20}` | JobApplicationTargetResponse | targetResumeId |

두 응답 모두 `id`, `jobId`, `status`, `appliedAt`을 포함한다. 기존 단일 JobApplicationResponse는 제거했다.
일반 지원의 JSON 필드는 유지되며, 특화 지원은 null이던 commonResumeId 대신 targetResumeId를 반환한다.
서비스 내부에서는 동일한 모양의 JobApplicationCommand를 재사용한다.

접수 조건은 삭제되지 않은 공고, OPEN 상태, 마감일 미지정 또는 **Asia/Seoul 기준 오늘 이상**이다.
일반·특화 지원에 동일하게 적용한다. 마감일 당일까지 가능하며 CLOSED나 DRAFT는 접수하지 않는다.
기존 본인 이력서 검증, 특화 이력서의 공고 일치 검증, 중복 지원 검증도 유지한다.

## 지원 취소

`DELETE /api/v1/jobs/{jobId}/applications` → 성공 204.

마감 여부와 관계없이 현재 본인의 지원을 취소할 수 있다는 기존 동작을 유지한다.
취소 시 status는 CANCELED, canceledAt이 기록된다. 이후 조회는 applied=false이며,
공고가 여전히 접수 가능하면 재지원할 수 있다. 과거 취소 기록은 삭제하지 않는다.

## 오류

JobController의 IllegalArgumentException은 다음 형태의 **HTTP 400**으로 응답한다.

```json
{"code":"INVALID_JOB_REQUEST","message":"이미 지원한 공고입니다."}
```

중복 지원, 유형 불일치, 타인 이력서, 접수 종료 등을 같은 상태 코드로 처리하며 message로 안내한다.
프론트는 메시지 문자열을 비교하여 별도의 상태 코드를 추론하지 않는다.
기존 Spring Security의 인증/인가 오류와 ResponseStatusException 상태는 유지한다.
다른 도메인에 영향을 주지 않도록 예외 핸들러는 JobController에만 적용한다.

## 동시 요청과 DB 적용

지원 및 취소는 트랜잭션 내에서 사용자 행에 PESSIMISTIC_WRITE 잠금을 획득한다.
동일 사용자 요청은 중복 조회부터 저장/취소까지 직렬화된다. 로컬 H2에서도 이 경로를 사용한다.

운영 PostgreSQL의 DB 차원 보장은 다음 수동 마이그레이션을 적용해야 한다.

`database/migrations/005_one_active_job_application.sql`

활성 `(job_id, user_id)` 조합의 부분 유일 인덱스를 생성한다. 취소 기록은 인덱스에서 제외한다.
스크립트의 사전 조회로 기존 중복을 확인한다. 중복이 있으면 인덱스 생성이 실패하며
기록을 임의로 삭제하지 않는다. 이번 코드 변경에서 실제 DB에 스크립트를 실행하지 않았다.
