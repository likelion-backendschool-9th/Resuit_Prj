# 기업 이메일 인증 (WBS 6.1·6.2)

사용자 결정: 이메일 도메인을 제한하지 않는다. 입력한 이메일의 코드를 확인하면 company_email 인증 완료로 기록한다. 실제 회사 재직 여부를 별도로 심사하는 기능은 아니다. users.role 변경·기업 권한 부여는 인증 담당자와 별도 계약이 필요하며 이번 작업에서는 자동 변경하지 않는다.

## 화면·API

- 화면: `/profile/verification`, 프로필의 기업 이메일 인증 버튼으로 이동.
- `POST /api/v1/verifications/email`, JSON: `{"email":"user@example.com"}`.
- 성공: verificationId, expiresAt, resendAfterSeconds(60).
- `POST /api/v1/verifications/email/confirm`, JSON: `{"verificationId":1,"code":"123456"}`.
- 성공: verificationId, email, verifiedAt.
- 두 API 모두 인증된 Principal 이름을 users.username으로 조회한다. 요청에서 사용자 ID나 권한을 받지 않는다.

## 처리 규칙

- SecureRandom 6자리 숫자, 5분 유효, 사용자별 60초 재발송 제한.
- 매번 임의 salt를 사용하며 서버 비밀키 기반 HMAC-SHA256으로 저장한다. 평문 코드는 메일에만 포함하고 응답·로그에는 노출하지 않는다.
- 5회 잘못 입력하면 더 이상 확인할 수 없다. 오입력 예외에서도 실패 횟수는 커밋한다.
- 재발송 성공 시 직전 미완료 코드를 만료시킨다. 확인 완료 코드는 재사용할 수 없다.
- 사용자 행의 쓰기 잠금으로 같은 사용자의 발급·확인을 직렬화한다.
- 인증 레코드 생성 시각이 저장되도록 JPA Auditing 설정을 추가했다. 기존 BaseCreatedEntity와 BaseTimeEntity에도 적용된다.
- `database/migrations/002_verification_attempts.sql`은 개인 DB에 적용 완료. 공용 DB도 배포 전에 컬럼 반영 필요.
- SMTP 실패 시 성공 응답·신규 인증 레코드를 남기지 않는다. 현재는 동기 발송 후 DB를 저장하므로 발송 직후 DB 커밋이 실패하면 받은 코드가 유효하지 않을 수 있다. 발송함/outbox를 사용하는 운영 구조는 별도 개선 사항이다.

오류 코드: 비로그인 401, 입력/코드 불일치 400, 타인 또는 없는 요청 404, 만료 410, 이미 완료 409, 재발송 간격/입력 횟수 초과 429, 발송 미설정·실패 503. 오류 응답은 code와 message다.

## SMTP 설정

SMTP는 인증 메일을 외부 수신함으로 보내는 메일 발송 서버다. 발송 계정/서비스가 정해지면 그 서비스의 값을 프로젝트 루트 .env에 로컬로 설정한다. 비밀번호와 키는 Git에 올리지 않는다.

```dotenv
SPRING_MAIL_HOST=발송서버주소
SPRING_MAIL_PORT=587
SPRING_MAIL_USERNAME=발송계정
SPRING_MAIL_PASSWORD='서비스에서 발급한 메일용 비밀번호'
SPRING_MAIL_PROPERTIES_MAIL_SMTP_AUTH=true
SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE=true
VERIFICATION_MAIL_FROM=발신이메일
VERIFICATION_CODE_SECRET=32자이상의충분히긴무작위비밀값
```

서비스별 포트·TLS·인증 설정은 발송 서비스 안내에 맞춰 조정한다. 비밀키를 바꾸면 이전 미완료 코드를 확인할 수 없으므로 재발급이 필요하다. SMTP 연결·읽기·쓰기에는 5초 타임아웃을 둔다. Compose backend에도 위 변수 전달을 추가했다. 로컬 Gradle 실행은 .env를 환경 변수로 로드하고 백엔드를 재시작한다.

현재 실제 SMTP 계정은 설정하지 않았다. 미설정 상태를 발송 성공으로 표시하거나 화면에 코드를 보여주는 우회 기능은 없다. 로그인 담당자도 아직 연결되지 않아 브라우저의 비인증 요청은 401이다.

## 검증

- 실제 개인 PostgreSQL과 임시 JavaMailSender 수신 대역으로 도메인 제한 없음, 메일에 코드 포함, DB 해시 저장, 재발송 간격, 타인 요청 차단, 오입력 횟수 커밋, 정상 확인, 재사용 차단, 5회 제한, 만료를 확인했다.
- 검증용 사용자·인증 레코드는 정리했다. 실제 외부 주소로 메일을 발송하지 않았다.
- 백엔드 컴파일·프론트 빌드 통과. 실제 SMTP 전달과 로그인된 브라우저 전체 흐름은 연동 후 확인해야 한다.

추가 검증: 미완료 인증을 재발송한 뒤 이전 요청의 정확한 코드를 제출해도 EXPIRED로 거부되고 새 요청이 별도 생성되는 것을 실제 개인 DB와 수신 대역으로 확인했다. 검증용 사용자와 인증 레코드는 정리했다.
