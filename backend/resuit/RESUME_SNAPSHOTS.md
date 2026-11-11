# 제출 이력서 스냅샷

일반·특화 지원은 기존 JobApplicationService에서 검증 후 제출 자료를 복사한다. 원본 변경·삭제는 제출 자료를 변경하지 않는다. 기존 지원 데이터는 보정하지 않는다. 기업 지원현황 페이지/API는 이 스냅샷을 사용한다. 기업용 조회 경로와 권한은 JOB_APPLICATION_API.md의 기업 지원현황 조회 절에 정리했다.

## 컬럼

- CommonResume / TargetResume: `photo_url VARCHAR(500) NULL`.
- JobApplication:
  - `submitted_resume_type VARCHAR(20)`: COMMON / TARGET. 원본 삭제·본문 만료 후에도 유지.
  - `submitted_company_name VARCHAR(100)`, `submitted_resume_title VARCHAR(100)`: 지원 이력 표시용.
  - `submitted_resume_snapshot JSONB`: schemaVersion=1, title, field, content, questions(id/content/charLimit/orderNo). 사진 파일 본체와 현재 엔티티는 넣지 않는다.
  - `snapshot_photo_key VARCHAR(500) NULL`: 비공개 보존 사진 파일 키. 사진이 없으면 NULL.
  - `snapshot_expires_at TIMESTAMP`: applied_at + 달력상 3개월.
  - `snapshot_deleted_at TIMESTAMP NULL`: 본문과 사진 정리 완료 시각.

`common_resume_id`, `target_resume_id`는 원본 삭제 시 SET NULL. target_resumes의 common_resume_id도 출처 참조만 해제한다. 지원 기록 자체와 사용자가 복원한 초안은 만료 시 삭제하지 않는다. 피드백 댓글이나 유료 피드백 등 다른 도메인의 삭제 제한은 계속 적용된다.

## 조회·복원 API

- `GET /api/v1/resumes`: 기존 공통/특화 목록에 `submittedResumes` 추가. 보존 기간 내 본인의 지원 자료(취소 내역 포함). applicationId, resumeType, title, companyName, restoreTitle, appliedAt, expiresAt를 반환한다. 화면에 필요한 목록을 기존 대표 API에 포함해 추가 요청을 피한다.
- `GET /api/v1/users/me/resume-submissions/{applicationId}`: 본인 제출 자료 상세. 사진은 인증이 필요한 photoUrl로 반환. 원본 파일 키는 공개하지 않는다.
- `GET /api/v1/users/me/resume-submissions/{applicationId}/photo`: 본인 보존 사진 바이너리. Cache-Control: no-store.
- `POST /api/v1/users/me/resume-submissions/{applicationId}/restore`: 본문 없는 요청. 201 `{id, resumeType, title}`. 같은 유형의 새 초안을 만든다. 기존 이력서를 덮어쓰지 않고 지원 상태도 바꾸지 않는다.
- 타인의 지원 내역은 404, 만료된 자료는 410. 모든 조회/복원에 사용자 소유권과 만료를 확인한다.

복원 이름은 `지원날짜_회사명_이력서명`이며 최대 100자로 제한한다. 특화 이력서는 기존 공고당 하나라는 제한을 유지하므로 해당 공고의 이력서가 이미 있으면 안내 후 거절한다. 삭제된 공고 또는 SPECIAL이 아닌 공고도 특화 복원을 거절한다. 복원된 content.submittedQuestions/submittedAnswers에는 당시 문항/답변을 보관하고 편집 화면에서 별도 표시한다. 현재 문항의 답변을 저장·재제출할 때는 현재 질문 ID와 글자 수를 검증한다.

ResumeController는 HTTP 처리를, ResumeService는 복원 조합을 담당한다. JobApplicationService는 제출/소유권/만료 검증을 담당하고, CommonResumeService/TargetResumeService가 새 초안을 생성한다. 기존 MediaService가 파일 복사·읽기·삭제를 담당한다.

## 사진

공통 생성/수정 및 특화 수정 DTO에 `photoUrl` 추가. 수정 시 null/생략은 유지, 빈 문자열은 제거한다. 업로드 API가 반환한 `tmpImage/{userId}/{filename}` 또는 본인의 `image/{userId}/{filename}` 및 `/media/image/...` 경로를 받는다. 외부 URL을 서버가 다운로드하지 않는다. 다음 사진 UI 브랜치도 이 저장 계약에 맞춰 연결해야 한다.

제출 시 `${resuit.media.root}/resumeSnapshots/{UUID}.{extension}`으로 파일을 복사한다. 이 폴더에는 공개 정적 리소스 매핑이 없으며 인증 API를 통해서만 읽는다. 원본 사진이 없어지더라도 제출 사진은 유지된다. 복원 시에는 새 일반 이미지 파일로 다시 복사하므로 제출 자료가 만료되어도 복원한 초안 사진은 유지된다. DB 트랜잭션 롤백 시 이번에 복사한 파일을 정리한다.

## 만료와 배포

ResumeSnapshotScheduler가 기본 10분 주기로 만료 행을 200건씩 조회하고 각 행을 별도 트랜잭션으로 정리한다. `resuit.snapshots.cleanup-cron`으로 주기를 변경할 수 있다. 만료 시각 이후에는 정리 작업 실행 전에도 API에서 접근을 차단한다. 파일 삭제 실패 시 DB 본문과 파일 키를 남겨 다음 실행에서 재시도한다. 정리가 끝나면 본문과 사진 키를 NULL로 하고 완료 시각을 기록한다. 지원·복원·삭제는 사용자 행 잠금, 복원·파일 열람·만료 정리는 지원 행 잠금으로 경합을 조정한다.

기존 DB에는 `database/migrations/007_resume_submission_snapshots.sql`을 적용한다. 로컬에서는 기존 `bash scripts/migrate-local-db.sh`가 이 파일도 포함한다. 새 스키마에는 엔티티 필드 및 OnDelete 매핑이 반영되지만, 기존 FK/CHECK 변경은 ddl-auto=update에 의존하지 않는다. 이 작업에서는 실제 DB에 SQL을 실행하지 않았다. 멀티 인스턴스 배포 시 기존 미디어와 동일하게 보존 파일이 모든 인스턴스에서 접근 가능한 영속 저장소에 있어야 한다.
