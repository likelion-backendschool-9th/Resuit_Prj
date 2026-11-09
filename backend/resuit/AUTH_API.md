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
