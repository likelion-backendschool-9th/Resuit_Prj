# JobController 화면 연동

## 구현 범위

- `/jobs`: 공개 공고 목록, 일반·특화 탭, 최신순, 20개 단위 페이지 이동.
- `/jobs/{id}`: 회사·채용 조건·본문·요구 문항, 작성자의 수정·삭제 진입.
- `/company/post`: 기업 계정의 공고 작성 및 임시저장 목록.
- `/company/post?draft={id}`: 본인 임시저장 불러오기, 수정 저장, 게시, 삭제.
- `/company/post?job={id}`: 공개된 본인 공고 수정.

서비스 함수는 `src/services/jobs.js`에 둔다. 공개 조회는 서버에서도 호출하여
첫 HTML과 상세 페이지 metadata에 내용을 반영한다. 요청 실패 시 클라이언트 재시도를 제공한다.
개인 임시저장은 세션을 사용하며, 쓰기는 기존 authenticatedFetch의 CSRF 처리를 재사용한다.
권한은 서버가 검증한다. 기업 전용 임시저장 조회가 거절되면 작성 폼을 표시하지 않는다.
인증 세션 응답에 역할이 없으므로 프론트에서 닉네임이나 사용자명으로 기업 여부를 추정하지 않는다.

공개 목록은 백엔드의 createdAt 내림차순 정렬을 그대로 사용한다.
목록 응답에 없는 전체 유형별 수, 회사 팔로워 수 등은 임의로 표시하지 않는다.
기존 AppLayout / MainColumn / RightAside를 유지하며 상단 탭은 페이지 내부 상태다.

## 지원 기능의 백엔드 계약 보완

지원 상태 조회, 마감 검증, 오류 응답, 일반·특화 응답 분리는 구현되었다.
상세 계약은 [지원 API 문서](../backend/resuit/JOB_APPLICATION_API.md)를 따른다.

- 본인 상태: `GET /api/v1/jobs/{jobId}/applications/me` (로그인 필요, 캐시 금지).
- 미지원 또는 취소 후: `applied: false`. 현재 지원: `resumeType`과 `resumeId`로 제출 이력서를 식별한다.
- 일반 지원 결과: `commonResumeId`, 특화 지원 결과: `targetResumeId`.
- 접수 조건: 삭제되지 않음, OPEN 상태, 한국 시간 기준 마감일 당일까지.
- 기존 IllegalArgumentException은 HTTP 400의 `code`, `message` 응답으로 변환한다.

공개 공고 상세와 개인별 지원 상태는 공개 범위와 캐시 정책이 다르므로 별도 API로 유지한다.
프론트엔드 지원·취소 UI 연결은 아직 수행하지 않았다. 기존 GeneralJobDetail의 메모리 기반
지원 동작을 새 상세 경로에서 호출하지 않는다.

## 검증

`frontend/`에서 `npm run build`. 실제 기업 세션과 백엔드를 사용하는 저장·게시 검증은 별도로 필요하다.

## 내가 작성한 공고

- 화면: `/company/jobs` (검색엔진 색인 제외), 기존 3단 서비스 레이아웃 유지.
- 왼쪽 ‘공고 관리’는 ‘내가 작성한 공고’와 ‘공고 작성’ 하위 메뉴로 구성한다.
- API: `GET /api/v1/jobs/me?page=0&size=20`, `Cache-Control: no-store`.
- JobController는 HTTP 입력·페이지 범위(0 이상 / 1~100)를 처리하고, JobService는 기업 권한과
  세션 username에 해당하는 사용자 ID를 확인한다. JobRepository가 본인 소유·미삭제 조건을 적용한다.
- 응답은 기존 `Page<JobResponse>`를 재사용한다. 임시저장·게시 중·마감 공고를 모두 포함하고
  updatedAt 내림차순, id 내림차순으로 정렬한다. 비로그인 401, 일반 회원 403.
- `/me/drafts`는 작성 화면의 임시저장 선택 용도로 유지한다. `/me`는 공고 관리 화면에서
  전체 상태를 조회하는 용도다. 공개 목록 `/jobs`는 다른 회사 공고도 포함하며 초안을 제외한다.
- 목록은 서버 페이지네이션을 사용한다. 임시저장은 편집 화면으로, 공개 공고는 상세·관리 화면으로 연결한다.
