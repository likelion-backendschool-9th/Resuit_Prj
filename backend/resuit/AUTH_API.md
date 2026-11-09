# 일반 회원가입과 로그인

기존 `UserRepository`와 `AuthService`를 확장한다. 회원 응답은 기존
`SessionMemberResponse`(`id`, `username`, `nickname`)를 재사용한다.
회원가입 화면은 추가하지 않는다.

## 기본 로그인 페이지

백엔드의 `GET /login`으로 접속하면 Spring Security가 생성한 로그인 화면이 나온다.
아이디(`username`)와 비밀번호로 로그인하며, 폼의 `POST /login`은 Security 필터가 처리한다.
성공 시 `resuit.auth.public-base-url`(기본값 `http://localhost:5173`)로 이동한다.
실패 시 `/login?error`로 돌아간다.

## JSON API

두 API는 `UserController` 하단에 있다. 기본 폼과 JSON API 모두 같은
`DaoAuthenticationProvider`와 `UserDetailsService`로 비밀번호를 검증한다.
JSON 로그인은 인증 성공 후 세션 ID를 교체하고 SecurityContext를 저장한다.
소셜 로그인은 기존 OAuth2 provider를 그대로 사용한다.

| 요청 | 본문 | 성공 |
| --- | --- | --- |
| `POST /api/v1/users/signup` | `username`, `email`, `password`, `nickname` | 201, 회원 정보 |
| `POST /api/v1/users/login` | `username`, `password` | 200, 회원 정보 및 로그인 세션 |

회원가입 예시:

```json
{
  "username": "resuit_user",
  "email": "user@example.com",
  "password": "example-password-123",
  "nickname": "새회원"
}
```

- 아이디: 영문·숫자·밑줄·하이픈, 3~50자. 중복은 409로 응답한다. 동시 가입은 DB의 기존 username 유일 제약으로 막는다.
- 이메일: 필수, 이메일 형식, 최대 255자. 로그인 식별자는 아이디이며 이메일의 유일성은 요구하지 않는다.
- 비밀번호: 8자 이상, UTF-8 기준 최대 72바이트. BCrypt로 해시하고 `{bcrypt}` 식별자를 함께 저장한다.
- 닉네임: 필수, 최대 50자. 역할은 서버에서 `USER`로 지정한다.
- 잘못된 요청은 400, 로그인 인증 실패는 401이다. 비밀번호가 없는 소셜 전용 계정은 비밀번호 로그인에 사용할 수 없다.
- 가입 후 자동 로그인하지 않는다. 가입한 아이디로 `/login` 또는 JSON 로그인 API를 사용한다.

## 세션과 CSRF

1. `GET /api/v1/auth/session`을 호출해 세션 쿠키와 `csrfHeader`, `csrfToken`을 받는다.
2. 같은 쿠키를 유지하고, POST 요청에 `Content-Type: application/json` 및 반환된 이름/값의 CSRF 헤더를 보낸다.
3. 로그인 성공 시 세션 ID와 CSRF 토큰이 갱신되므로 `/api/v1/auth/session`을 다시 호출한다.
4. 이후 요청에도 세션 쿠키를 전달한다. 로그아웃은 기존 `POST /api/v1/auth/logout`과 최신 CSRF 토큰을 사용한다.

기본 로그인 폼에는 Security가 CSRF 필드를 넣으므로 직접 토큰을 전달할 필요가 없다.
세션 조회와 로그아웃은 기존 `/api/v1/auth` API를 재사용하여 중복 API를 만들지 않는다.

현재 `local` 프로필은 기존 개발용 `X-Username` 인증을 켜고 CSRF를 끈다.
실제 인증·CSRF 동작을 확인할 때는 `--app.dev-header-auth.enabled=false`로 실행한다.

세션 구현 참고: [Spring Security 세션 관리](https://docs.spring.io/spring-security/reference/servlet/authentication/session-management.html).


## 소셜 회원의 비밀번호 로그인 추가

`POST /api/v1/auth/me/password` — 인증된 세션과 CSRF 토큰이 필요하다.

```json
{"password":"example-password-123"}
```

- 소셜 로그인한 뒤 `/setting/integrations`의 ‘아이디·비밀번호 로그인’에서 설정한다.
- 로그인 아이디는 기존 username을 그대로 사용한다. 새 회원 생성, username 변경, 데이터 소유자 변경은 하지 않는다.
- 기존 password 컬럼에 BCrypt 해시만 저장하므로 추가 DB 마이그레이션이 없다.
- 8자 이상, UTF-8 기준 최대 72바이트. 첫 설정만 가능하며 이미 설정했다면 409다.
- 소셜 로그인 수단이 없는 계정에는 400을 반환한다. 비로그인은 Security가 401로 차단한다.
- 성공 응답은 `AuthAccountResponse`: `user`(기존 SessionMemberResponse), `linkedProviders`, `passwordLoginEnabled`.
- 같은 상태는 기존 `/api/v1/auth/session`에도 포함한다. 별도 상태 조회 API를 만들지 않는다.
- 비밀번호를 설정해도 현재 세션과 users.id가 유지된다. 로그아웃 후 기존 username과 새 비밀번호로 로그인한다.
- 비밀번호 변경·재설정이나 분리된 두 회원의 데이터 병합은 이번 범위에 포함하지 않는다.

## 계정 연결·해제

다른 회원이 소유한 소셜 계정은 `already_linked` OAuth 오류로 거절한다.
콜백 실패는 기존 핸들러를 통해 `/setting/integrations?error=already_linked`로 전달한다.
소셜 계정 엔티티의 소유자 변경 메서드는 제거했다.
기존 사용자의 username을 OAuth 로그인 시 자동 교체하던 로직도 제거했다.

새 소셜 계정의 연결, 해제, 첫 비밀번호 설정은 동일한 UserRepository 사용자 행 잠금을 사용한다.
따라서 동시에 마지막 두 소셜 계정을 해제하거나 비밀번호를 두 번 설정하는 요청도 직렬화된다.
PostgreSQL에서는 일반 OAuth 로그인과 연결 시 provider+subject에 대해 같은 advisory lock을 사용해
같은 외부 계정의 동시 최초 등록을 직렬화한다. H2에는 해당 잠금 함수가 없으며 기존 DB 유일 제약을 사용한다.

## 인증 코드 책임

| 기존 파일 | 책임 |
| --- | --- |
| UserController | 기존 가입·로그인 URL 유지, DTO 검증, 인증 성공 후 HTTP 세션·SecurityContext 저장 |
| AuthController | 세션 조회 응답, OAuth 시작 URL·쿠키, 로그인 수단 추가·해제 HTTP 엔드포인트. 레포지토리를 직접 호출하지 않음 |
| AuthService / AuthServiceImpl | 가입·비밀번호 검증/해시, AuthenticationManager 호출, 계정 상태 조회, 첫 비밀번호 설정, 연결 시작 검증, 마지막 로그인 수단 보호·해제 트랜잭션 |
| SocialMemberService / SocialMemberServiceImpl | OAuth 사용자 정보 식별, 연결 의도·현재 인증 사용자 확인, 소셜 회원 생성·연결, 다른 소유자 연결 차단 |
| LocalUserDetailsServiceImpl | 기존 사용자 저장소에서 Spring Security 비밀번호 인증 정보를 제공 |
| UserRepository / SocialAccountRepository | 사용자·소셜 계정 조회·저장·행 잠금. HTTP 및 화면 처리 없음 |

새 컨트롤러·서비스·레포지토리를 만들지 않았다. 새 파일은 입력 모양이 다른 PasswordSetupRequest,
비밀번호 해시를 노출하지 않는 AuthAccountResponse와 화면 전용 PasswordSetupForm만 추가했다.
