# Google·GitHub·네이버 소셜 로그인

## 동작과 현재 상태

실제 제공자의 OAuth 인증 후 내부 회원을 연결하고 서버 세션으로 로그인한다. 기존 목업의 버튼 클릭만으로 로그인되는 동작은 제거했다. GitHub 앱 키를 로컬 .env에 설정해 로그인 시작을 활성화했다. Google·네이버는 각각 키가 설정되기 전까지 준비 중으로 표시한다.

- 시작: `/api/oauth2/authorization/{google|github|naver}`.
- 콜백: `/api/login/oauth2/code/{provider}`. 브라우저 공개 주소를 기준으로 제공자에 등록한다.
- 성공: `/`로 이동. 실패·잘못된 state: `/onboarding?error=oauth`.
- `GET /api/v1/auth/session`: 로그인 여부, 본인 id/username/nickname, 활성 제공자 목록, CSRF 헤더명·토큰. Cache-Control: no-store.
- `POST /api/v1/auth/logout`: CSRF 토큰 확인 후 세션 무효화, 204.
- JSESSIONID는 HttpOnly, SameSite=Lax, 기본 30분 세션이다. 운영 HTTPS에서는 Secure 쿠키와 신뢰하는 프록시의 forwarded header 설정도 맞춰야 한다.
- 기본 프로필·회원 식별 범위만 요청한다. 저장소 수정·비공개 레포·메일 발송 권한을 로그인에 요청하지 않는다.
- 신규 회원의 `username`은 GitHub `login`, Google 이메일 로컬파트, 네이버 닉네임 등을 소문자·허용 문자로 정리해 쓰고, 중복이면 `_1` 접미사를 붙인다. 예전에 `u_`+UUID로 만들어진 계정은 다음 로그인 때 같은 규칙으로 한 번 바꾼다.

## 앱 등록과 키 설정

앱 등록은 각 서비스에 “이 사이트가 로그인을 요청한다”는 정보를 등록하는 과정이다. 사용자 비밀번호를 Resuit가 받지 않고 제공자의 로그인·동의 화면으로 이동한다.

로컬 공개 주소가 `http://localhost:5173`인 경우:

| 제공자 | 개발자 설정 | 등록할 콜백 URL |
| --- | --- | --- |
| Google | Google Cloud/Google Auth Platform에서 웹 애플리케이션 OAuth 클라이언트 생성 | http://localhost:5173/api/login/oauth2/code/google |
| GitHub | Settings → Developer settings → OAuth Apps → New OAuth App | http://localhost:5173/api/login/oauth2/code/github |
| 네이버 | NAVER Developers → 애플리케이션 등록 → 네이버 로그인·PC 웹 설정 | http://localhost:5173/api/login/oauth2/code/naver (콜백·서비스 URL을 위와 정확히 일치. Spring Security 7 PKCE는 네이버 등록에서 비활성화함) |

홈페이지/서비스 URL은 `http://localhost:5173`을 사용한다. Google이 테스트 상태라면 이용할 테스트 계정을 등록하고, 네이버도 개발/검수 상태에 맞는 이용 계정을 확인한다. 운영 도메인이 정해지면 그 도메인의 콜백 주소를 별도로 등록한다. 제공자의 콜백 등록 값과 실제 redirect_uri는 일치해야 한다.

공식 안내:

- [Google 웹 서버 OAuth](https://developers.google.com/identity/protocols/oauth2/web-server)
- [GitHub OAuth App 등록](https://docs.github.com/en/apps/oauth-apps/building-oauth-apps/creating-an-oauth-app)
- [네이버 로그인 개발 가이드](https://developers.naver.com/docs/login/devguide)

발급받은 값은 루트 .env에만 입력한다.

```dotenv
RESUIT_AUTH_PUBLIC_BASE_URL=http://localhost:5173
GOOGLE_CLIENT_ID=발급값
GOOGLE_CLIENT_SECRET='발급값'
GITHUB_CLIENT_ID=발급값
GITHUB_CLIENT_SECRET='발급값'
NAVER_CLIENT_ID=발급값
NAVER_CLIENT_SECRET='발급값'
```

설정한 제공자부터 사용 가능하며, 모든 제공자를 동시에 등록할 필요는 없다. Client Secret을 채팅·Git·프론트 환경 변수에 넣지 않는다. 설정 후 백엔드를 재시작한다. `localhost`와 `127.0.0.1`을 섞으면 세션 쿠키가 공유되지 않을 수 있으므로 브라우저도 등록 주소로 연다.

## 내부 회원·보안 계약

- database/migrations/004_social_accounts.sql을 배포 전에 적용한다. 개인 DB에는 적용 완료. users.username 유일성도 확인한다. 기존 중복 회원을 자동 병합하지 않는다.
- social_accounts의 provider+subject 유일키로 외부 계정을 식별한다. 같은 외부 계정의 반복 로그인은 같은 users 회원에 연결된다.
- 제공자가 다른 계정은 이메일이 같아도 자동 병합하지 않는다. 온보딩에서 Google만 쓰거나 GitHub만 쓰면 각각 별도 Resuit 회원이 된다.
- **계정 연결**: 로그인한 뒤 `/setting/integrations`에서 `POST /api/v1/auth/link/{provider}`가 서명된 `link` 토큰이 붙은 인가 주소를 돌려준다. 그 토큰은 인가 시작 시 OAuth state와 같은 세션에 저장되어, 콜백에서 현재 회원에 `social_accounts` 행을 추가한다. 프로필에서 연결하지 않고 다른 제공자로 외부 로그인하면 별도 계정이다. 이미 다른 회원에 묶인 제공자·subject를 연결하면 `already_linked` 오류로 차단한다. 로그인 수단의 소유자와 기존 회원 데이터는 변경하지 않는다. `DELETE /api/v1/auth/link/{provider}`로 해제할 수 있으나 비밀번호도 없고 소셜 계정도 하나뿐이면 해제할 수 없다. 비밀번호 로그인이 설정되어 있으면 마지막 소셜 계정도 해제할 수 있다.
- `GET /api/v1/auth/session`의 `linkedProviders`에 현재 회원에 연결된 제공자 목록이 포함되고, `passwordLoginEnabled`로 비밀번호 설정 여부를 알 수 있다.
- 첫 로그인에는 임의의 유일 username과 USER 역할을 생성한다. 외부 프로필 값을 내부 역할이나 권한으로 사용하지 않는다.
- Principal.getName()은 내부 users.username이다. 기존 리뷰·이메일 인증 서비스는 이 계약을 그대로 사용한다.
- 동일 외부 계정의 첫 로그인을 DB 트랜잭션 advisory lock으로 직렬화한다.
- Google은 profile/email OAuth 사용자 정보의 sub, GitHub는 id, 네이버는 response.id를 사용한다. 이메일은 회원 자동 연결이나 기업 이메일 인증 완료 판정에 사용하지 않는다.
- OAuth 토큰·외부 사용자 비밀번호를 social_accounts에 저장하지 않는다. 프레임워크의 인증 과정과 서버 측 OAuth 클라이언트 저장소에서만 취급하며 프론트에 반환하지 않는다.
- 세션 기반 CSRF 보호는 활성화한다. 프론트의 authenticatedFetch는 변경 요청 전에 세션 endpoint에서 토큰을 받아 지정 헤더로 전달한다. 로그인·로그아웃 이후 이전 토큰을 계속 사용하지 않는다.
- 세션 조회는 전역 메뉴·계정 상태·여러 기능의 변경 요청에서 쓰이므로 프로필 전용 API와 분리했다.
- OAuth 이동은 전체 문서 이동이 필요한 외부 인증 흐름이므로 로그인 버튼은 일반 anchor를 사용한다. 일반 서비스 내 이동은 기존 Link를 유지한다.
- GitHub 로그인·연결도 `/api/oauth2/authorization/github`다. 인가 주소에 `prompt=select_account`를 붙여 GitHub 계정 선택 화면을 거친 뒤, 등록된 콜백 `http://localhost:5173/api/login/oauth2/code/github`로 Resuit에 돌아온다. `github.com/logout`으로는 보내지 않는다. GitHub는 그 주소의 외부 복귀를 버리고 홈에 남긴다.

## 기존 화면의 범위

로그인 상태와 메뉴 계정명은 실제 세션을 사용한다. GitHub 레포 선택·블로그 연결은 실제 API가 준비되지 않아 기존 가짜 완료 단계를 로그인 화면에서 제거했다. 해당 연동 기능은 별도 구현이 필요하다. 피드·이력서 등의 기존 mock 데이터까지 이번 작업에서 DB로 전환한 것은 아니다.

소셜 로그인과 SMTP는 별개다. Gmail로 로그인할 수 있어도 인증 메일 발송 권한이 생기지는 않으며 [SMTP 설정](company-verification.md)이 필요하다.

## 검증 한계

실제 개인 DB로 세 제공자 프로필 매핑, 반복 로그인, 같은 이메일 계정의 분리, 동시 첫 로그인, USER 역할 생성을 검증했다. 임시 회원·계정은 검증 후 정리했다. 외부 제공자의 실제 코드 교환·동의 화면은 앱 키가 없어 아직 확인하지 않았다. 화면에서 로그인 우회를 제공하지 않는다.

추가 통합 검증: 임시 MockMvc에 실제 보안 필터와 개인 DB를 연결해 세 제공자의 OAuth 리다이렉트·state 생성, 잘못된 콜백 state 차단, 검증용 로그인 세션의 내부 회원 조회, CSRF 없는 변경 요청 403, 올바른 CSRF를 포함한 좋아요 실제 저장, 로그아웃 204와 세션 무효화를 확인했다. 이 검증은 테스트 안에서 세션 인증 정보를 구성한 것이며 제공자의 실제 로그인·코드 교환을 수행한 것은 아니다. 프로덕션 코드에 인증 우회는 추가하지 않았다. 실행 중인 프론트에서도 익명 세션·키 미설정 제공자 3개·비로그인 변경 요청 401을 확인했다. 백엔드 컴파일·프론트 빌드 통과.

### OAuth 복귀 주소 수정

프록시 환경에서 성공 핸들러의 상대 경로 `/`가 백엔드 주소로 해석되어 홈 404로 이어지는 문제를 수정했다. LoginRedirectHandler는 로그인 성공·실패 모두 RESUIT_AUTH_PUBLIC_BASE_URL의 절대 주소로 돌려보낸다. 앱 설정으로만 목적지를 정하고 요청 파라미터의 이동 주소는 사용하지 않는다. 성공/실패 절대 경로 검증과 기존 세션·CSRF·좋아요 저장·로그아웃 통합 검증을 수행했다.


### 로그인 완료 화면의 이전 오류 표시 수정

로그인 세션이 확인되어 닉네임·피드 이동 버튼이 표시돼도 이전 `/onboarding?error=oauth`의 오류 문구가 남는 문제를 수정했다. 로그인 상태에서는 실패 문구를 표시하지 않고 URL의 error 파라미터도 제거한다. 세션 확인 중에는 이전 OAuth 오류 표시를 보류하며, 실제 실패한 비로그인 상태에서만 안내한다. 로그인 후에는 로그인 없이 둘러보기 링크도 숨긴다.
