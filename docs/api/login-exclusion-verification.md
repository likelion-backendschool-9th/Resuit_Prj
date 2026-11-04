# 로그인 구현 제외본 검증

검증일: 2026-11-04 (Asia/Seoul).

## 결과

현재 작업 폴더를 변경하지 않고 `/tmp/resuit-without-login-50hc5hla`에 별도 검증본을 만들었다. .env는 복사하지 않았다. 독립된 node_modules를 사용했고 현재 실행 서버·DB·로그인 세션은 변경하지 않았다.

- backend/resuit: `bash gradlew compileJava --offline` 성공.
- frontend: `npm run build` 성공. 리뷰·명예의 전당·기업 이메일 인증 라우트 생성 확인.
- Java 소스에서 Spring Security 및 소셜 로그인 전용 클래스 참조가 남지 않음을 확인.
- 프론트 소스에서 auth.js·authenticatedFetch·세션 조회 함수 참조가 남지 않음을 확인.
- 설정·실행 스크립트에 OAuth 키·004 migration·제외한 소셜 로그인 문서 참조가 남지 않음을 확인.

컴파일·빌드에 이어 아래 별도 실행 환경에서 Chromium 브라우저 검증까지 완료했다. 실제 로그인 없이 좋아요·채택·메일 발송을 성공시키는 우회 코드는 추가하지 않았다. 해당 기능은 기존 Principal 연동을 기다리는 형태다. HEAD의 기존 로그인 화면과 AppStateContext는 목업이므로 서버 인증과 혼동하지 않아야 한다.

## 제외한 로그인 전용 파일

- config/SecurityConfiguration.java
- config/SocialClientConfiguration.java
- config/LoginRedirectHandler.java
- controller/AuthController.java
- entity/SocialAccount.java
- repository/SocialAccountRepository.java
- service/SocialMemberService.java
- frontend/src/services/auth.js
- database/migrations/004_social_accounts.sql
- docs/api/social-login.md

Java 경로는 backend/resuit/src/main/java/com/newlecture/resuit/ 기준이다.

## 파일 내부에서 분리할 변경

- build.gradle: Security·OAuth2 Client 의존성 제거. 메일·JPA는 유지.
- 리뷰·기업 인증 프론트 서비스: authenticatedFetch 대신 일반 fetch, same-origin credentials 유지.
- AppStateContext·OnboardingPage·OnboardingPage.css·SettingsMenu: HEAD 상태로 복원.
- compose.yml·.env.example: OAuth 키·공개 인증 주소 설정 제외, SMTP·DB 설정 유지.
- migration 스크립트: 001~003까지만 적용.
- README·상태·실행 문서: 로그인 구현 완료 설명과 제외 문서 링크 제거, Principal 연동 대기로 표시.
- 프론트 ProfilePage의 기업 이메일 인증 진입 버튼, 리뷰 메뉴·화면, JPA Auditing·선정 Clock 설정은 로그인 제외본에도 유지.

사용자가 기존에 수정한 AGENTS.md·.gitignore와 untracked frontend/AGENTS.md도 검증본의 기능 변경에서 제외했다. 이 파일들을 로그인 기능으로 분류한다는 뜻은 아니며 별도 의도 없이 커밋에 포함하지 않기 위한 처리다.

## 보존·푸시 상태

원본 작업 폴더의 로그인 코드는 보존했다. Git index를 변경하지 않았고, 커밋·푸시는 하지 않았다. 현재 작업 폴더 자체가 로그인 제외본으로 바뀐 것은 아니다. 실제 커밋 전에 위 분리 변경을 선택적으로 준비하고 최종 diff를 확인해야 한다. 임시 검증 폴더는 /tmp 정리 시 사라질 수 있으므로 이 문서를 재현 기준으로 삼는다.


## 실제 브라우저 검증 완료

Playwright + Headless Chromium으로 로그인 제외본을 직접 조작했다. 브라우저 도구는 /tmp/resuit-browser-check에만 설치했으며 프로젝트 의존성을 변경하지 않았다. 검증 대상은 별도 프론트 http://127.0.0.1:5175, 백엔드 localhost:8082다. DB는 개인 PostgreSQL을 사용하되 ddl-auto=validate, 자동 선정 비활성화로 실행했다. 원본 프론트 5174·백엔드 8081의 로그인 구현은 그대로 유지했다.

통과 항목:

- 주간 리뷰 3건과 좌측 메뉴·우측 영역 표시.
- 월간 탭 클릭으로 실제 API와 본문 변경. URL·좌측 위치·공통 문서 유지.
- 비인증 좋아요 POST의 실제 401 응답, 로그인 안내, 좋아요 수·미선택 상태 보존.
- 인증되지 않은 요청자에게 채택 버튼이 노출되지 않음.
- 명예의 전당 1·2·3페이지 20/20/14건 표시, 이전·다음 경계에서 버튼 비활성화, 뒤로 이동 정상.
- Link 이동에서 window에 둔 검증 마커 유지로 전체 문서 새로고침 없이 화면이 전환됨 확인.
- 프로필의 기업 이메일 인증 링크 이동, 실제 메일 API 401, 오류 안내 표시. 발송 성공이나 코드 입력창을 거짓으로 표시하지 않음.
- 390px 모바일 인증 화면에서 가로 넘침 없음. 스크린샷 육안 확인.
- 조회 오류 안내 후 다시 시도를 누르면 실제 DB 조회로 복구. 이 오류 조건에만 브라우저 응답 대역을 사용함.
- 빈 목록 안내 정상. 빈 목록 조건에만 응답 대역을 사용했으며 DB 데이터는 지우지 않음.
- 로그인 전용 API 호출 없음. pageerror로 수집한 클라이언트 JavaScript·하이드레이션 오류 없음.

증거 파일(임시 폴더):

- /tmp/resuit-browser-check/result.json
- /tmp/resuit-browser-check/best-desktop.png
- /tmp/resuit-browser-check/hall-desktop.png
- /tmp/resuit-browser-check/verification-mobile.png

로그인 제외본이므로 로그인 사용자의 실제 좋아요 저장·채택·메일 수신 성공까지 검증한 결과는 아니다. 기존 401 처리가 유지되는지 확인했다. .env·OAuth 키·기존 로그인 세션은 브라우저 검증 컨텍스트에 복사하지 않았다. 커밋·푸시는 하지 않았다.
