# 기업 이메일 인증 (WBS 6.1·6.2)

기업용 이메일 도메인(`company_email_domains`)에 등록된 도메인의 이메일을 입력하면, 메일 발송이나 코드 확인 없이 바로 인증을 마치고 그 회원의 `users.role`을 `COMPANY`로 바꾼다. 등록되지 않은 도메인은 400 `DOMAIN_NOT_ALLOWED`로 거부한다. 기업 권한(인가)은 `role`로 판단한다. 이메일 소유 여부나 실제 재직 여부는 확인하지 않는다. 도메인 목록과 초기 데이터는 [기업 이메일 도메인 마이그레이션](../migrations/2026-11-06-company-email-domains.sql)에 있다.

## 화면·API

- 화면: `/profile/verification`, 프로필의 기업 이메일 인증 버튼으로 이동.
- `GET /api/v1/verifications/me` → 내 기업 이메일 인증 상태 (`type`, `status`, `email`, `verifiedAt`).
  - `status`: `NONE` | `VERIFIED`. 완료 기록이 있으면 `VERIFIED`다.
- `POST /api/v1/verifications/email`, JSON: `{"email":"user@newlecture.com"}`.
  - 성공: `verificationId`, `email`, `verifiedAt`. 이때 `users.role`이 `COMPANY`가 된다.
- 인증 API는 인증된 Principal 이름을 `users.username`으로 조회한다. 상태 조회는 로그인 사용자(세션 또는 `X-Username`) 기준이다. 요청에서 사용자 ID나 권한을 받지 않는다.

## 처리 규칙

- 이메일은 앞뒤 공백을 지우고 `@` 뒤를 소문자로 바꾼 뒤, `@` 뒤 도메인이 목록에 정확히 일치할 때만 통과한다. 하위 도메인(예: `mail.newlecture.com`)은 따로 등록해야 한다.
- 인증할 때마다 `verifications`에 완료 기록(`verified_at`)을 남긴다. 코드가 없으므로 `code_hash`는 `NONE`, `expires_at`은 인증 시각으로 저장한다.
- 사용자 행의 쓰기 잠금으로 같은 사용자의 인증 요청을 하나씩 처리한다.
- 도메인을 추가하려면 `company_email_domains`에 `@` 없이 소문자로 넣는다.

오류 코드: 비로그인 401, 이메일 형식 오류·등록되지 않은 도메인 400. 오류 응답은 `code`와 `message`다.

## 변경 이력

- 2026-11-06: 메일 발송, 6자리 코드 확인(`POST /email/confirm`), 재발송 제한, 입력 실패 횟수, SMTP 설정을 제거했다. 등록된 도메인 이메일을 입력하면 바로 기업 회원으로 전환한다. `verifications.failed_attempts` 컬럼은 남아 있지만 더 이상 쓰지 않는다.
