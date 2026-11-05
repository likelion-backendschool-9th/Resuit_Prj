# 프로필·배지 API

모든 경로는 `/api/v1` 아래에 있다. 공개 프로필 응답에는 통계, 대표 이력서, 대표 배지, 보유 배지와 지난 배지 이력이 함께 들어 있다. 비밀번호와 연동 토큰은 응답에 포함하지 않는다.

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
| 대표·공개 레포 조회 | `GET /users/{username}/github/repos?pinned=true` (DB 핀) / `?pinned` 없음 (GitHub 공개 레포) |
| 대표 레포 수정 | `PUT /users/me/github/pinned-repos`, `{ "repoFullNames": ["owner/repo"] }` (최대 3개) |
| 기여 잔디 조회 | `GET /users/{username}/github/contributions` (GitHub GraphQL, 최근 1년) |
| 최근 블로그 글 | `GET /users/{username}/blog-posts?size=5` |
| 연동 상태 | `GET /users/me/integrations` |
| 블로그 연결·해제 | `PUT /users/me/integrations/blog`, `DELETE /users/me/integrations/{provider}` |
| 공통 이력서 CRUD | `GET/POST /resumes/common-resumes`, `GET/PATCH/DELETE /resumes/common-resumes/{id}` |

GitHub로 로그인하면 액세스 토큰과 login을 `integrations`(GITHUB)에 저장한다. `pinned=true`는 DB의 대표 레포만, 파라미터 없으면 연동된 계정의 공개 레포를 GitHub API로 가져와 `pinned` 여부를 함께 표시한다. 기여 잔디는 GraphQL로 최근 1년 기록을 가져온다. 블로그 RSS는 연결된 Velog·Tistory 주소를 매시 15분에 수집한다.

프로필 수정(`PATCH /users/me`)의 `widgets`는 `{ "grass": true, "resumes": true }` 형태다. 대표 레포 선택은 `/setting/integrations`에서 한다.

OAuth 세션 로그인과 `local` 프로필의 `X-Username` 헤더를 본인 식별에 쓴다. 로컬 실행은 다음과 같다.

```bash
cd backend/resuit
SPRING_PROFILES_ACTIVE=local ./gradlew bootRun
```

로컬 프로필은 H2 메모리 DB와 예시 사용자 `seoyeon_dev`를 만든다. 프론트 프로필·연동 화면은 목업 없이 API만 사용한다. `frontend/`에서 `npm run dev`로 실행하고 `/profile`, `/setting/profile`, `/setting/integrations`에서 확인할 수 있다.
