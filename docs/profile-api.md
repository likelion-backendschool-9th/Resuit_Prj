# 프로필·배지 API

모든 경로는 `/api/v1` 아래에 있다. 공개 프로필 응답에는 통계, 경력, 대표 이력서, 대표 배지, 보유 배지와 지난 배지 이력이 함께 들어 있다. 비밀번호와 연동 토큰은 응답에 포함하지 않는다.

| 기능 | 경로 |
| --- | --- |
| 내 프로필 조회·수정 | `GET/PATCH /users/me` |
| 공개 프로필 조회 | `GET /users/{username}` |
| 팔로우·언팔로우 | `POST/DELETE /users/{username}/follow` → `{ isFollowing, followerCount, followingCount }` |
| 게시글·기술 글 | `GET /users/{username}/tweets?page=&size=&type=tech` |
| 남긴 피드백 | `GET /users/{username}/feedbacks?page=&size=` |
| 대표 이력서 선택·해제 | `PUT /users/me/pinned-resume`, `{ "cResumeId": 1 }` 또는 `null` |
| 이력서 공개 설정 | `PUT /users/me/resumes/{resumeId}/visibility`, `{ "isPublic": true }` |
| 대표 배지 선택·해제 | `PUT /users/me/displayed-badge`, `{ "badgeId": 1 }` 또는 `null` |
| 리뷰어 배지 진행 현황 | `GET /badges/me/reviewer-progress` |
| 내 기업 이메일 인증 상태 | `GET /verifications/me` → `{ type, status, email, verifiedAt }` (본인만, 이메일 포함) |
| 대표·공개 레포 조회 | `GET /users/{username}/github/repos?pinned=true` (DB 핀) / `?pinned` 없음 (GitHub 공개 레포) |
| 대표 레포 수정 | `PUT /users/me/github/pinned-repos`, `{ "repoFullNames": ["owner/repo"] }` (최대 3개) |
| 기여 잔디 조회 | `GET /users/{username}/github/contributions?from=&to=` (GitHub GraphQL. 생략 시 최근 1년, 지정 시 `YYYY-MM-DD`·최대 1년) |
| 최근 블로그 글 | `GET /users/{username}/blog-posts?size=5` |
| 연동 상태 | `GET /users/me/integrations` |
| GitHub 동기화 | `POST /users/me/integrations/github/sync` → 저장된 토큰으로 login·`last_synced_at` 갱신 |
| 블로그 소유 검증 시작 | `POST /users/me/integrations/blog/verify` → Velog·ETC만. `{ platform, blogId?, rssUrl? }` → `{ code, expiresAt, hint, ... }`. 세션에만 보관(ERD 없음). RSS가 열려야 발급 |
| 블로그 소유 검증 확인 | `POST /users/me/integrations/blog/verify/confirm` → RSS·프로필에서 `resuit-verify-…` 코드 확인 후 연결·수집 |
| 티스토리 로그인 연결 | 사용하지 않음. Open API 종료(인가 주소 404). 티스토리도 위 검증 코드로 연결 |
| 블로그 직접 연결 | `PUT /users/me/integrations/blog` → 미검증 연결 불가(400). 위 검증 API 사용 |
| 블로그 동기화 | `POST /users/me/integrations/blog/sync` → 저장된 RSS로 재수집·`last_synced_at` 갱신 |
| 블로그·GitHub 해제 | `DELETE /users/me/integrations/{provider}` (`blog` 또는 `github`) → 행 유지, `connected=false`, GitHub는 `access_token` 삭제 |
| 공통 이력서 CRUD | `GET/POST /resumes/common-resumes`, `GET/PATCH/DELETE /resumes/common-resumes/{id}` |

GitHub로 로그인하면 액세스 토큰과 login을 `integrations`(GITHUB)에 저장한다. `access_token`은 `INTEGRATION_TOKEN_SECRET`으로 AES-GCM 암호화해 DB에 두고, GitHub API 호출 시에만 복호화한다. API 응답에는 토큰을 포함하지 않는다. `pinned=true`는 DB의 대표 레포만, 파라미터 없으면 연동된 계정의 공개 레포를 GitHub API로 가져와 `pinned` 여부를 함께 표시한다. 기여 잔디는 GraphQL로 가져온다. `from`·`to`를 생략하면 최근 1년이고, 둘 다 보내면 그 기간(최대 365일)을 조회한다. 응답에 실제 조회 구간 `from`·`to`와 `weeks`·`totalContributions`가 포함된다. 프로필 화면에서는 연도(숫자)를 고르며, 해당 해의 1.1~12.31(올해는 오늘까지)을 조회한다. 블로그 RSS는 Velog·Tistory·ETC(직접 URL)를 지원한다. 연결 전 소유 검증 코드(`resuit-verify-…`)를 소개/글에 넣어야 한다. 벨로그 소개는 공개 GraphQL에서 읽고, 티스토리는 `https://{id}.tistory.com/rss`와 블로그 첫 화면에서 코드를 찾는다. 티스토리 Open API는 2024년 2월에 종료되어 로그인 연결은 쓰지 않는다. 이후 수동 sync와 매시 15분 스케줄로 수집한다. 사설·내부망 RSS는 거부한다.

프로필 조회 응답에는 기업 이메일 인증 공개 플래그 `companyVerified`와 `companyVerifiedAt`이 있다. `verifications`에 `company_email` 완료 기록이 있으면 `true`다. 이메일은 넣지 않는다. 화면에서는 닉네임 옆에 “현직자 인증” 배지를 보여 준다. 리뷰어 `displayedBadge`·`badges`와는 별개다.

프로필 조회 응답의 `careers`는 `user_careers`를 표시 순서대로 담는다. 한 줄은 `companyName`, `jobTitle`, `startDate`, `endDate`, `description`, `displayOrder`다. `endDate`가 없으면 재직 중이다. 프로필 수정(`PATCH /users/me`)은 `nickname`, `selfIntroduction`, `profileImageUrl`, `widgets`, `careers`를 받는다. `careers`를 생략하면 기존 경력을 유지하고, 배열을 보내면 그 목록으로 통째로 바꾼다. 빈 배열은 경력을 모두 지운다. 최대 20개다. `widgets`는 `{ "grass": true, "resumes": true }` 형태다. 프로필 사진은 `POST /posts/uploads`로 이미지를 올린 뒤 받은 임시 경로를 `profileImageUrl`에 넣거나, `https://` 주소를 그대로 보낸다. 임시 경로는 저장 시 `image/`로 옮기고 `/media/...`로 보관한다. 닉네임·소개·사진 편집 UI는 `/setting/profile`, 위젯·대표 이력서는 프로필 꾸미기, 대표 레포는 `/setting/integrations`에서 한다.

OAuth 세션 로그인과 `local` 프로필의 `X-Username` 헤더를 본인 식별에 쓴다. 로컬 실행은 다음과 같다.

```bash
cd backend/resuit
SPRING_PROFILES_ACTIVE=local ./gradlew bootRun
```

로컬 프로필은 H2 메모리 DB와 예시 사용자 `seoyeon_dev`를 만든다. 프론트 프로필·연동 화면은 목업 없이 API만 사용한다. `frontend/`에서 `npm run dev`로 실행하고 `/profile`, `/setting/profile`, `/setting/integrations`에서 확인할 수 있다.
