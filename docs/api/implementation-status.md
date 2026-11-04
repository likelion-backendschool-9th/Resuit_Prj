# 담당 기능 구현·팀 연동 현황

기준: 현재 엔티티와 사용자 ERD(ai짬떄리기.sql). docs/erdcloud/resuit_erd_import.sql은 이번 구현 기준에서 제외했다.

## WBS 대응

| WBS | 구현 | 남은 연동 |
| --- | --- | --- |
| 6.1 기업 이메일 인증 요청 | API, 메일 발송 연결부, 코드 해시·만료·재발송 제한, 화면 | 실제 SMTP 발송 계정·소셜 로그인 앱 키 |
| 6.2 기업 이메일 인증 확인 | API, 소유자 확인·횟수 제한·재사용 방지, 화면 | 실제 메일 수신·외부 소셜 로그인 브라우저 확인 |
| 7.1 베스트 리뷰 목록 | 최신 선정 결과 조회 API·화면 | 자동 선정 서비스·스케줄러 구현 완료, 공용 DB migration·운영 스케줄 검증 필요 |
| 7.2 기간별 조회 | WEEKLY/MONTHLY 탭·API 필터 | 합의한 Asia/Seoul 기간 경계 적용 |
| 7.3 좋아요 등록·취소 | API, 개인 DB 저장, 중복·동시 요청 처리, 화면 | 외부 소셜 로그인 키 설정 후 브라우저 전체 흐름 |
| 7.4 채택 리뷰 | 요청자만 요청당 한 개 채택, 잠금·DB 제약, 화면 | 피드백 작성 담당자의 실제 DB 데이터·외부 로그인 검증 |
| 7.5 인기 리뷰 | 제공 WBS와 기존 설계 문서에 따라 7.1과 같은 best 조회 API 사용 | 실시간 좋아요 정렬을 별도 기능으로 원하면 API·선정 기준부터 합의 필요 |
| 7.6 명예의 전당 | 전체 선정 이력 조회, 페이지 나누기, 화면 | 운영 선정 데이터 |

현재 베스트 리뷰는 DB에 이미 저장된 선정 결과를 읽는다. 좋아요를 눌러도 저장된 rank_no는 즉시 재계산하지 않는다. 선정 인원·평가 기준·동점 처리·집계 기간은 2026-11-04 사용자 승인으로 확정했고, 자동 선정 서비스와 스케줄러를 구현했다. 상세 기준은 review-likes.md의 자동 선정 기준 항목을 따른다.

## 다른 담당자에게 필요한 계약

1. 인증: 검증된 Principal.getName()으로 users.username을 전달한다. 사용자 UUID를 전달하는 방식으로 결정되면 리뷰·기업 인증의 조회 경계를 함께 수정한다. 브라우저의 mock 로그인 상태는 서버 인증이 아니다.
2. 피드백: feedback_comments.comment_id / feedback_id / user_id 관계를 사용한다. 화면의 mock ID를 그대로 서버에 보내지 않는다. 한 요청당 채택 한 개 제약은 다른 경로의 채택 구현에도 적용해야 한다.
3. DB: 공용 DB는 현재 엔티티와 스키마를 먼저 비교한다. database/migrations/001_one_accepted_review.sql, 002_verification_attempts.sql, 003_best_review_selection.sql, 004_social_accounts.sql은 수동 적용 파일이며 자동 migration 실행기는 없다. 기존 중복 채택을 삭제하거나 공용 DB에 ddl-auto=update를 적용하지 않는다.
4. 기업 기능: 도메인은 제한하지 않고 이메일 수신 확인만 company_email 인증 완료로 기록한다. users.role은 자동 변경하지 않는다. 기업 기능 인가에 무엇을 사용할지 기업 담당자와 정해야 한다.
5. 메일: SMTP 정보·발신 주소·코드 비밀키를 설정하고 실제 수신을 확인한다. 설정은 company-verification.md 참고.

## 현재 로컬 환경

- PostgreSQL: localhost:5433/resuit. 개인 Docker 볼륨 사용.
- Backend: http://localhost:8081, ddl-auto=validate.
- Frontend: http://localhost:5174, BACKEND_URL=http://localhost:8081.
- 리뷰: /reviews/best, /reviews/hall-of-fame.
- 기업 이메일 인증: /profile/verification.
- 프론트 API 요청은 Next.js 프록시를 통해 실제 백엔드·개인 DB에 연결한다.
- database/local/review-demo.sql은 개발용 가상 기록이다. 54개 리뷰와 12주·6개월의 선정 이력이 있으며 공용·운영 DB에 적용하지 않는다.

## 검증 범위

- 컴파일·프론트 빌드, 페이지 및 프론트 API 프록시 HTTP 응답 확인.
- 실제 Spring 서비스와 개인 PostgreSQL로 채택 권한·중복·동시성 검증.
- 자동 선정은 실제 개인 DB로 순위·기간 경계·후보 부족·완료 기록·동시 실행을 검증했다. 실제 자정 예약 시각의 운영 실행은 별도 확인이 필요하다.
- 메일은 임시 수신 대역으로 발급·확인·만료·횟수·재발송을 검증한다. 외부 메일은 발송하지 않았다.
- 로그인된 실제 브라우저의 전체 사용자 흐름, 외부 SMTP 전달, 운영 배포는 아직 확인하지 않았다.

상세 API: [리뷰](review-likes.md), [기업 이메일 인증](company-verification.md).

## 소셜 로그인 범위 추가

로그인 담당자가 없어 Google·GitHub·네이버 세션 로그인을 이 작업에 포함했다. 내부 username Principal 계약·CSRF·세션 메뉴를 구현했고, 실제 제공자 로그인 앱 등록과 키 설정이 남아 있다. [소셜 로그인 상세](social-login.md)를 참고한다.
