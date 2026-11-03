# Resuit Proejct

## 로컬 PostgreSQL 데이터베이스 준비

루트 `.env` 파일을 준비한 후 다음 명령어를 실행합니다.

```bash
docker compose up db
```

단순 docker compose up만 쓰면 프론트 백도 다 올라갑니다.

# Resuit 웹 (Next.js 프로토타입)

이력서 피드백 SNS **Resuit**의 프론트엔드 프로토타입입니다.
Next.js 16(App Router) + React 19로 만들었고, Tailwind는 쓰지 않았습니다. 스타일은 일반 CSS 파일과 CSS 변수(디자인 토큰)로 작성했습니다.
지금은 `frontend/src/data/mock.js`의 예시 데이터로 동작하며, 백엔드 API가 준비되면 이 부분만 교체하면 됩니다.

## 실행

```bash
cd frontend
npm install
npm run dev        # http://localhost:5173
npm run build      # 운영용 빌드 (.next/)
npm start          # 빌드 결과 실행 (port 5173)
```

Node.js 20.9 이상이 필요합니다.

Docker로 실행 중이라면 의존성이 바뀌었을 때 옛 `node_modules` 볼륨을 새로 만들어야 합니다.

```bash
docker compose up -d --build -V
```

프론트엔드 소스, Next.js 설정, 의존성 및 패키지 스크립트는 모두 `frontend/`에 있습니다. 백엔드가 추가되면 백엔드 설정과 의존성도 별도의 `backend/`에 둡니다.

## 폴더 구조

```
frontend/
├─ package.json
├─ package-lock.json
├─ next.config.mjs
├─ jsconfig.json                  `@/` 경로 별칭
├─ public/feed/                   피드 예시 사진 · 영상
└─ src/
   ├─ app/                        라우트 정의 (layout · page · metadata)
   │  └─ (service)/               왼쪽 메뉴가 있는 3단 레이아웃 페이지 묶음
   ├─ views/                      화면 단위 컴포넌트 (아래 표)
   ├─ styles/
   │  ├─ tokens.css               색 · 반경 · 폰트 토큰 (라이트/다크)
   │  └─ global.css               기본 스타일 + 공통 유틸 클래스
   ├─ data/mock.js                예시 데이터 (게시글 · 공고 · 이력서 · 피드백 · 스레드)
   ├─ context/
   │  ├─ ThemeContext.jsx         다크 모드 상태
   │  └─ AppStateContext.jsx      화면 간 공유 상태 (로그인 · 피드 · 팔로우 · 지원 · 피드백 요청 · 답변)
   └─ components/
   │  ├─ layout/                  AppLayout(3단) · LeftNav · SettingsMenu
   │  ├─ common/                  Icon · Avatar · Badge · Button · Tabs · Chip · FollowButton · SearchBox · ThemeToggle
   │  ├─ feed/                    Composer(글쓰기) · PostCard · PostEmbeds(태그/블로그/레포/근거)
   │  ├─ jobs/                    JobCard
   │  ├─ feedback/                FeedbackRequestCard
   │  └─ profile/                 ContributionGraph(GitHub 잔디)
```

컴포넌트마다 같은 이름의 `.css` 파일이 옆에 있습니다. 클래스 이름은 `컴포넌트__요소`, 상태는 `is-active`/`is-on` 형태로 씁니다.

## 화면 (라우트)

| 경로 | 페이지 | 주요 동작 |
|---|---|---|
| `/onboarding` | OnboardingPage | GitHub 로그인 → 대표 레포 선택 → 블로그 RSS 연결. 로그아웃하면 이 화면으로 옴 |
| `/` | FeedPage | 상단 탭 고정(추천 · 팔로잉 · 기술 정보 · 질문), 글쓰기(일반 · 기술 정보 · 질문 + 코드 첨부), 사진 · 영상 · 코드 블록 글, 좋아요, 팔로우, 오른쪽은 검색만 |
| `/jobs` | JobsPage | 전체 · 일반 · 특화 공고. 카드에서 공고 상세로 이동 |
| `/jobs/[id]` | JobDetailPage | 특화: 요구 문항 확인, 작성 방식(공통 이력서 기반 / 처음부터) 선택. 일반: 공통 이력서를 골라 지원 · 지원 취소 |
| `/resumes/edit` | ResumeEditPage | 특화 이력서 작성. `?job=1&mode=base` 또는 `mode=blank` |
| `/resumes` | ResumesPage | 이력서 › 내 이력서. 공통 이력서 여러 개 추가 · 이름 변경 · 삭제. "피드백 요청" 시 피드백 게시판에 실제로 올라감 |
| `/feedback` | FeedbackPage | 이력서 › 피드백. 섹션을 골라 피드백 작성, 내 요청의 피드백 채택 |
| `/threads/[id]` | ThreadPage | 질문 스레드, 답변 작성, 도움돼요 |
| `/profile` | ProfilePage | GitHub 잔디, 프로필 꾸미기(위젯 켜기/끄기), 공개 이력서 설정(최대 3개), 공유자 배지, 리뷰어 인증 배지 진행 현황 |
| `/company/post` | CompanyPostPage | 일반 · 특화 공고 작성, 요구 문항 추가 · 수정 · 삭제 |
| `/talent` | TalentSearchPage | 기업 회원 전용 인재 찾기. 분야별 조회는 무료, 배지 · 기술 · 경력 · 활동 근거 · 지역 필터는 기업 Pro(체험) |

공통 동작:
- **왼쪽 메뉴**: "이력서"를 누르면 하위 메뉴(내 이력서 · 피드백)가 펼쳐져요.
- **설정 메뉴**: 프로필 편집, 연동 관리, 로그아웃이 있어요.
- **다크 모드 토글**: 로고 옆에 있어요.
- **사이드바 고정**: 왼쪽·오른쪽 사이드바는 스크롤해도 따라와요.

## 수정 가이드

- **색**: `frontend/src/styles/tokens.css`의 변수만 고칩니다. 화면 코드에는 색상값을 직접 쓰지 않아요. 다크 모드는 `[data-theme='dark']` 블록에서 같은 이름을 덮어써요.
- **메뉴**: `frontend/src/components/layout/LeftNav.jsx` 위쪽의 `ITEMS_TOP`, `RESUME_CHILDREN`, `ITEMS_BOTTOM` 배열을 고칩니다.
- **예시 데이터**: `frontend/src/data/mock.js`
- **공유 상태와 이후 API 연동**: `frontend/src/context/AppStateContext.jsx`. 각 함수(`addPost`, `toggleFollow`, `applyJob`, `toggleResumeRequest`, `addFeedback` 등)를 API 호출로 바꾸면 됩니다.
- **라우트 추가**: `src/views/`에 화면을 만들고(`'use client'`), `src/app/(service)/주소/page.jsx`에서 불러옵니다. 검색 결과와 링크 미리보기에 나올 제목은 이 파일의 `metadata`로 정합니다.
- **렌더링**: 첫 화면은 서버에서 HTML로 만들어집니다. 렌더링 중에 `window`, `localStorage` 등을 읽으면 오류가 나니 `useEffect`에서 읽으세요.

## 백엔드(3계층) 연동 시 참고

- **게시글**: 하나의 테이블에 `type`(general · tech · question · blog · repo · job)으로 구분해요.
- **댓글**: 답변, 피드백, 댓글은 댓글 테이블 하나로 통일해요. 피드백은 `section` 컬럼을 추가로 둬요.
- **이력서**: 공통 이력서 1개와 특화 이력서 N개(공고별)로 나눠요. 특화 이력서는 `base_resume_id`(공통 기반이면 값 있음)를 가져요.
- **팔로우**: `follow(follower_id, following_id)` 테이블로 처리해요.
- **피드백 요청**: `resume_feedback_request(resume_id, message, focus_sections, field)` 테이블로 처리해요.
- **GitHub**: OAuth 토큰으로 GraphQL `contributionsCollection`(잔디)과 REST 레포 목록을 가져와요.
- **블로그**: `data/mock.js`의 `RSS_RULES` 주소 규칙대로 스케줄러가 주기적으로 수집해요.
