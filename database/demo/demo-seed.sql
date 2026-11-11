-- 시연용 데이터: 지원자·기업 회원, 공고, 공통·특화 이력서, 지원, 피드백 게시판, 피드 글·댓글·좋아요
--
-- 실행 (서버의 배포 폴더에서):
--   docker compose --env-file .env -f compose.yml -f compose.prod.yml exec -T db \
--     sh -c 'psql -v ON_ERROR_STOP=1 -U "$POSTGRES_USER" -d "$POSTGRES_DB"' < database/demo/demo-seed.sql
--
-- - 시연 회원은 이메일이 모두 @demo.resuit.kr 이다. 다시 실행하면 시연 회원의 데이터를 지우고 처음 상태로 되돌린다.
--   (리허설 중에 만든 지원·피드백도 함께 사라진다)
-- - 같은 아이디를 실제 회원이 이미 쓰고 있으면 아무것도 바꾸지 않고 멈춘다.
-- - 실제 회원이 시연 회원의 글에 댓글·팔로우 등을 남겼다면 지우기에서 멈춘다(그 경우 실제 데이터가 지워지지 않는다).
-- - 시연 계정 비밀번호는 모두 같다. 평문은 이 파일에 남기지 않고 따로 전달한다.
-- - 시간은 실행 시각 기준으로 계산하므로 시연 전날 실행하면 "3일 전"처럼 자연스럽게 보인다.
-- - 회사와 사람은 모두 가상이다.

SET client_encoding = 'UTF8';
-- 백엔드(TZ=Asia/Seoul)가 저장하는 시각과 맞춘다. 안 맞추면 "2시간 전"이 "11시간 전"으로 보인다.
SET TIME ZONE 'Asia/Seoul';
BEGIN;

CREATE TEMP TABLE demo_users (
    username    varchar(50) PRIMARY KEY,
    nickname    varchar(50) NOT NULL,
    role        varchar(20) NOT NULL,
    region      varchar(20),
    intro       text,
    joined_days int NOT NULL
) ON COMMIT DROP;

INSERT INTO demo_users VALUES
-- 지원자
('jiwon-kim',      '김지원', 'USER', 'SEOUL',    '트래픽이 몰리는 순간에도 흔들리지 않는 서버를 만들고 싶은 백엔드 개발자 지망생입니다.', 140),
('seojun.park',    '박서준', 'USER', 'GYEONGGI', '사용자가 헤매지 않는 화면을 고민하는 프론트엔드 개발자입니다. React와 접근성에 관심이 많아요.', 120),
('harin-lee',      '이하린', 'USER', 'SEOUL',    '디자인 시스템과 인터랙션을 좋아하는 프론트엔드 개발자 지망생입니다.', 110),
('yunho-choi',     '최윤호', 'USER', 'SEOUL',    '1년차 백엔드 개발자. 결제 도메인과 정산 배치를 다뤘습니다.', 200),
('daeun-jeong',    '정다은', 'USER', 'INCHEON',  'Next.js와 Spring으로 처음부터 끝까지 서비스를 만들어 보는 풀스택 개발자입니다.', 95),
('minjae-kang',    '강민재', 'USER', 'DAEJEON',  '데이터가 많아질수록 재미있어지는 백엔드 개발자 지망생입니다. 쿼리 튜닝을 좋아해요.', 88),
('soah-yoon',      '윤소아', 'USER', 'SEOUL',    '부트캠프 수료 후 프론트엔드 신입 포지션을 준비하고 있습니다.', 60),
('jihoon-han',     '한지훈', 'USER', 'SEOUL',    '커머스 회사 3년차 백엔드 개발자. 이력서 리뷰와 멘토링을 꾸준히 하고 있습니다.', 300),
('yerin-song',     '송예린', 'USER', 'GYEONGGI', '4년차 프론트엔드 개발자. 신입 개발자 포트폴리오 리뷰를 즐겨 합니다.', 320),
('taeyang-lim',    '임태양', 'USER', 'BUSAN',    '사이드 프로젝트를 서비스로 키워 보는 중인 풀스택 개발자입니다.', 45),
('seungwoo-oh',    '오승우', 'USER', 'GYEONGGI', 'Java와 Spring으로 백엔드를 공부하고 있는 컴퓨터공학과 4학년입니다.', 30),
('gaeun-baek',     '백가은', 'USER', 'SEOUL',    '비전공 출신 프론트엔드 개발자 지망생. 기록하면서 성장하고 있어요.', 75),
-- 기업
('finroad',        '핀로드',          'COMPANY', 'SEOUL',    '모두가 쉽게 쓰는 금융을 만드는 핀테크 스타트업입니다. 간편 송금과 자산 관리 서비스를 운영합니다.', 400),
('moakitchen',     '모아키친',        'COMPANY', 'SEOUL',    '동네 반찬가게와 고객을 잇는 푸드 커머스 플랫폼입니다.', 380),
('bridgelabs',     '브릿지랩스',      'COMPANY', 'GYEONGGI', '중소 제조사를 위한 B2B 주문·재고 관리 SaaS를 만듭니다.', 360),
('orbit-mobility', '오르빗모빌리티',  'COMPANY', 'SEOUL',    '전동 킥보드와 공유 자전거를 운영하는 모빌리티 기업입니다.', 420);

-- 1. 실제 회원이 같은 아이디를 쓰고 있으면 멈춘다
DO $$
DECLARE taken text;
BEGIN
    SELECT string_agg(u.username, ', ') INTO taken
    FROM users u JOIN demo_users d ON d.username = u.username
    WHERE u.email IS NULL OR u.email NOT LIKE '%@demo.resuit.kr';
    IF taken IS NOT NULL THEN
        RAISE EXCEPTION '실제 회원이 이미 쓰는 아이디가 있어 시연 데이터를 넣지 않았어요: %', taken;
    END IF;
END $$;

-- 2. 이전에 넣은 시연 데이터를 지운다 (다시 실행해도 처음 상태가 되게)
CREATE TEMP TABLE old_users ON COMMIT DROP AS
    SELECT id FROM users WHERE email LIKE '%@demo.resuit.kr';
CREATE TEMP TABLE old_requests ON COMMIT DROP AS
    SELECT id FROM feedback_requests WHERE user_id IN (SELECT id FROM old_users);
CREATE TEMP TABLE old_jobs ON COMMIT DROP AS
    SELECT id FROM job WHERE company_id IN (SELECT id FROM old_users);

DELETE FROM notifications WHERE user_id IN (SELECT id FROM old_users) OR actor_id IN (SELECT id FROM old_users);
CREATE TEMP TABLE old_posts ON COMMIT DROP AS
    SELECT id FROM posts WHERE user_id IN (SELECT id FROM old_users);
DELETE FROM post_like WHERE user_id IN (SELECT id FROM old_users) OR post_id IN (SELECT id FROM old_posts);
DELETE FROM comments WHERE parent_id IS NOT NULL
  AND (post_id IN (SELECT id FROM old_posts) OR user_id IN (SELECT id FROM old_users));
DELETE FROM comments WHERE post_id IN (SELECT id FROM old_posts) OR user_id IN (SELECT id FROM old_users);
DELETE FROM post_hashtags WHERE post_id IN (SELECT id FROM old_posts);
DELETE FROM post_tech_tags WHERE post_id IN (SELECT id FROM old_posts);
DELETE FROM posts WHERE id IN (SELECT id FROM old_posts);
DELETE FROM feedback_comment_likes
WHERE user_id IN (SELECT id FROM old_users)
   OR comment_id IN (SELECT comment_id FROM feedback_comments WHERE feedback_id IN (SELECT id FROM old_requests));
DELETE FROM feedback_comments WHERE parent_id IS NOT NULL
  AND (feedback_id IN (SELECT id FROM old_requests) OR user_id IN (SELECT id FROM old_users));
DELETE FROM feedback_comments
WHERE feedback_id IN (SELECT id FROM old_requests) OR user_id IN (SELECT id FROM old_users);
DELETE FROM feedback_request_sections WHERE request_id IN (SELECT id FROM old_requests);
DELETE FROM feedback_requests WHERE id IN (SELECT id FROM old_requests);
DELETE FROM job_applications WHERE user_id IN (SELECT id FROM old_users) OR job_id IN (SELECT id FROM old_jobs);
DELETE FROM target_resumes WHERE user_id IN (SELECT id FROM old_users) OR job_id IN (SELECT id FROM old_jobs);
DELETE FROM job_questions WHERE job_id IN (SELECT id FROM old_jobs);
DELETE FROM job WHERE id IN (SELECT id FROM old_jobs);
UPDATE users SET pinned_common_resume_id = NULL WHERE id IN (SELECT id FROM old_users);
DELETE FROM common_resumes WHERE user_id IN (SELECT id FROM old_users);
DELETE FROM user_careers WHERE user_id IN (SELECT id FROM old_users);
DELETE FROM follows WHERE follower_id IN (SELECT id FROM old_users) OR following_id IN (SELECT id FROM old_users);
DELETE FROM users WHERE id IN (SELECT id FROM old_users);

-- 3. 회원
INSERT INTO users (id, username, email, password, role, nickname, self_introduction, region, subscription, created_at, updated_at)
SELECT gen_random_uuid(), d.username, d.username || '@demo.resuit.kr',
       '{bcrypt}$2a$10$3PleSKCU/Eh1LWMpAH.KWuBiQfcnTseogpduGtaJw10tH.3Q5u1TS',
       d.role, d.nickname, d.intro, d.region, false,
       LOCALTIMESTAMP - make_interval(days => d.joined_days),
       LOCALTIMESTAMP - make_interval(days => d.joined_days)
FROM demo_users d;

CREATE TEMP TABLE u ON COMMIT DROP AS
    SELECT username AS name, id FROM users WHERE email LIKE '%@demo.resuit.kr';

-- 경력 (현직자·경력자)
INSERT INTO user_careers (user_id, company_name, job_title, start_date, end_date, description, display_order, created_at, updated_at)
SELECT u.id, c.company, c.title, c.start_date::date, c.end_date::date, c.descr, c.ord, LOCALTIMESTAMP - INTERVAL '90 days', LOCALTIMESTAMP - INTERVAL '90 days'
FROM (VALUES
    ('jihoon-han', '마켓플레이스코리아', '백엔드 개발자', '2023-09-01', NULL, '주문·결제 API와 쿠폰 시스템을 담당하고 있습니다. 주문 조회 API 응답 시간을 1.2초에서 180ms로 줄였습니다.', 0),
    ('jihoon-han', '스몰스텝', '백엔드 인턴', '2023-01-02', '2023-06-30', '사내 어드민 API를 Spring Boot로 이전했습니다.', 1),
    ('yerin-song', '데일리노트', '프론트엔드 개발자', '2022-03-02', NULL, '디자인 시스템을 만들고 웹뷰 성능을 개선했습니다. Lighthouse 성능 점수 52 → 91.', 0),
    ('yunho-choi', '페이링크', '백엔드 개발자', '2025-07-01', '2026-08-31', '정산 배치를 Spring Batch로 다시 만들어 처리 시간을 40분에서 7분으로 줄였습니다.', 0)
) AS c(username, company, title, start_date, end_date, descr, ord)
JOIN u ON u.name = c.username;

-- 팔로우
INSERT INTO follows (follower_id, following_id, created_at)
SELECT a.id, b.id, LOCALTIMESTAMP - make_interval(days => f.days)
FROM (VALUES
    ('jiwon-kim', 'jihoon-han', 40), ('jiwon-kim', 'yunho-choi', 25), ('jiwon-kim', 'minjae-kang', 12),
    ('seojun.park', 'yerin-song', 50), ('harin-lee', 'yerin-song', 44), ('soah-yoon', 'yerin-song', 20),
    ('gaeun-baek', 'yerin-song', 33), ('minjae-kang', 'jihoon-han', 28), ('seungwoo-oh', 'jihoon-han', 10),
    ('seungwoo-oh', 'jiwon-kim', 8), ('daeun-jeong', 'taeyang-lim', 15), ('taeyang-lim', 'daeun-jeong', 14),
    ('yunho-choi', 'jihoon-han', 60), ('soah-yoon', 'harin-lee', 18), ('harin-lee', 'soah-yoon', 17),
    ('jihoon-han', 'yerin-song', 90), ('yerin-song', 'jihoon-han', 89)
) AS f(follower, following, days)
JOIN u a ON a.name = f.follower
JOIN u b ON b.name = f.following;

-- 4. 공통 이력서 (회원마다 하나, 공개)
INSERT INTO common_resumes (user_id, title, field, content, is_public, created_at, updated_at)
SELECT u.id, r.title, r.field, r.content::jsonb, true,
       LOCALTIMESTAMP - make_interval(days => r.days), LOCALTIMESTAMP - make_interval(days => r.days - 3)
FROM (VALUES
('jiwon-kim', '대용량 트래픽을 고민하는 백엔드 개발자 김지원', 'BACKEND', 30, $j${
  "basic": {"name": "김지원", "school": "한빛대학교 컴퓨터공학과 (2027.02 졸업 예정)", "github": "github.com/jiwon-kim"},
  "skills": ["Java", "Spring Boot", "JPA", "MySQL", "Redis", "Docker", "AWS EC2", "GitHub Actions"],
  "projects": [
    {"id": "p-jiwon-ticket", "title": "콘서트 좌석 예매 서비스 (팀 4명, 백엔드 리드)", "desc": "오픈 직후 1분에 3,000건 이상 몰리는 예매 요청에서 같은 좌석이 두 번 팔리는 문제가 있었습니다. Redis 분산 락과 대기열을 도입하고 nGrinder로 부하 테스트를 반복해 중복 예매 0건, 평균 응답 210ms를 유지했습니다."},
    {"id": "p-jiwon-market", "title": "교내 중고거래 앱 API", "desc": "Spring Boot와 JPA로 게시글·채팅·거래 API를 만들었습니다. 목록 조회에서 N+1 문제를 fetch join과 배치 사이즈로 해결해 쿼리 수를 41개에서 3개로 줄였습니다. 실사용자 600명."},
    {"id": "p-jiwon-oss", "title": "오픈소스 기여 - 국내 결제 SDK", "desc": "타임아웃 설정이 무시되는 버그를 재현하는 테스트와 수정 PR을 보내 머지되었습니다."}
  ],
  "awards": [{"title": "한빛대학교 캡스톤디자인 경진대회 최우수상", "date": "2026.06"}, {"title": "정보처리기사", "date": "2026.09"}],
  "introduction": "문제가 생기면 숫자로 확인하고 숫자로 해결하는 개발자입니다. 좌석 예매 프로젝트에서 중복 예매를 막기 위해 락 방식 세 가지를 직접 비교하고, 부하 테스트 결과를 팀 위키에 정리해 다음 기수 팀이 그대로 가져다 쓸 수 있게 했습니다."
}$j$),
('seojun.park', '사용자 흐름을 설계하는 프론트엔드 개발자', 'FRONTEND', 26, $j${
  "basic": {"name": "박서준", "school": "경기대학교 소프트웨어학과 졸업", "github": "github.com/seojun-park"},
  "skills": ["TypeScript", "React", "Next.js", "React Query", "Zustand", "Storybook", "Jest"],
  "projects": [
    {"id": "p-seojun-travel", "title": "여행 일정 공유 서비스 '같이가'", "desc": "일정 편집 화면에서 드래그 앤 드롭 순서 변경과 실시간 동시 편집을 구현했습니다. 낙관적 업데이트로 체감 지연을 줄이고, 충돌 시 마지막 수정자를 보여 주는 UI를 만들었습니다."},
    {"id": "p-seojun-a11y", "title": "공공 웹사이트 접근성 개선 프로젝트", "desc": "스크린리더 사용자 인터뷰 5회를 바탕으로 키보드 탐색 순서와 대체 텍스트를 고쳐 웹 접근성 진단 점수를 68점에서 94점으로 올렸습니다."}
  ],
  "awards": [{"title": "웹 접근성 해커톤 장려상", "date": "2026.04"}],
  "introduction": "기능이 동작하는 것보다 사용자가 막힘없이 끝까지 가는 것이 더 중요하다고 생각합니다."
}$j$),
('harin-lee', '디자인 시스템을 좋아하는 프론트엔드 개발자', 'FRONTEND', 24, $j${
  "basic": {"name": "이하린", "school": "서울여자대학교 디지털미디어학과 졸업", "github": "github.com/harin-lee"},
  "skills": ["JavaScript", "React", "styled-components", "Figma", "Framer Motion", "Vite"],
  "projects": [
    {"id": "p-harin-ds", "title": "동아리 디자인 시스템 'Pebble'", "desc": "버튼·입력·모달 등 24개 컴포넌트를 Storybook으로 문서화하고 npm 패키지로 배포했습니다. 동아리 프로젝트 3개가 도입해 화면 개발 기간이 평균 30% 줄었습니다."},
    {"id": "p-harin-recipe", "title": "레시피 추천 웹앱", "desc": "냉장고 재료를 고르면 레시피를 추천합니다. 이미지 지연 로딩과 가상 스크롤로 첫 화면 로딩을 3.1초에서 1.4초로 줄였습니다."}
  ],
  "awards": [],
  "introduction": "디자이너와 같은 언어로 이야기할 수 있는 개발자가 되고 싶어 Figma와 디자인 토큰을 꾸준히 공부하고 있습니다."
}$j$),
('yunho-choi', '결제·정산 도메인 1년차 백엔드 개발자', 'BACKEND', 50, $j${
  "basic": {"name": "최윤호", "school": "충남대학교 컴퓨터공학과 졸업", "github": "github.com/yunho-choi"},
  "skills": ["Java", "Kotlin", "Spring Boot", "Spring Batch", "PostgreSQL", "Kafka", "Datadog"],
  "projects": [
    {"id": "p-yunho-settle", "title": "가맹점 정산 배치 재구축 (페이링크)", "desc": "하루 40만 건 거래 정산 배치를 청크 처리와 파티셔닝으로 다시 만들어 40분 걸리던 작업을 7분으로 줄였습니다."},
    {"id": "p-yunho-refund", "title": "부분 환불 API", "desc": "카드사별 부분 환불 규칙을 전략 패턴으로 분리해 신규 카드사 연동 기간을 2주에서 3일로 단축했습니다."}
  ],
  "awards": [],
  "introduction": "돈이 오가는 시스템에서 숫자 하나 틀리지 않는 코드를 쓰는 것을 가장 중요하게 생각합니다."
}$j$),
('daeun-jeong', '기획부터 배포까지 혼자 해 보는 풀스택 개발자', 'FULLSTACK', 22, $j${
  "basic": {"name": "정다은", "school": "인하대학교 정보통신공학과 졸업", "github": "github.com/daeun-jeong"},
  "skills": ["TypeScript", "Next.js", "Tailwind CSS", "Spring Boot", "PostgreSQL", "Vercel", "AWS"],
  "projects": [
    {"id": "p-daeun-study", "title": "스터디 출석 관리 서비스 '출첵'", "desc": "Next.js와 Spring Boot로 혼자 기획부터 배포까지 진행했습니다. 스터디 42개, 사용자 380명이 쓰고 있으며 출석 알림을 웹 푸시로 보냅니다."},
    {"id": "p-daeun-ci", "title": "GitHub Actions 배포 자동화", "desc": "PR마다 미리보기 환경을 띄우고 머지되면 운영에 배포되는 파이프라인을 만들었습니다."}
  ],
  "awards": [{"title": "SW중심대학 공동해커톤 우수상", "date": "2025.11"}],
  "introduction": "사용자 피드백을 받고 다음 날 고쳐서 배포하는 짧은 주기를 좋아합니다."
}$j$),
('minjae-kang', '쿼리 한 줄까지 고민하는 백엔드 개발자', 'BACKEND', 20, $j${
  "basic": {"name": "강민재", "school": "한밭대학교 컴퓨터공학과 4학년", "github": "github.com/minjae-kang"},
  "skills": ["Java", "Spring Boot", "QueryDSL", "MySQL", "Elasticsearch", "Redis"],
  "projects": [
    {"id": "p-minjae-search", "title": "대학 강의평 검색 서비스", "desc": "강의평 12만 건을 Elasticsearch로 옮기고 형태소 분석기를 적용해 검색 정확도를 높였습니다. LIKE 검색 대비 응답 시간이 2.4초에서 90ms로 줄었습니다."},
    {"id": "p-minjae-index", "title": "게시판 조회 성능 개선", "desc": "실행 계획을 분석해 복합 인덱스를 추가하고 커서 기반 페이지네이션으로 바꿔 깊은 페이지 조회를 30배 빠르게 만들었습니다."}
  ],
  "awards": [{"title": "SQLD", "date": "2026.03"}],
  "introduction": "느린 화면을 보면 원인을 찾을 때까지 실행 계획을 들여다보는 편입니다."
}$j$),
('soah-yoon', '부트캠프 출신 프론트엔드 신입 윤소아', 'FRONTEND', 14, $j${
  "basic": {"name": "윤소아", "school": "코드스테이션 프론트엔드 부트캠프 수료", "github": "github.com/soah-yoon"},
  "skills": ["JavaScript", "React", "Redux Toolkit", "HTML", "CSS", "Git"],
  "projects": [
    {"id": "p-soah-pet", "title": "반려동물 산책 메이트 매칭", "desc": "지도 API로 주변 산책 메이트를 보여 주고 채팅으로 약속을 잡는 서비스입니다. 팀 프로젝트에서 지도·채팅 화면을 맡았습니다."},
    {"id": "p-soah-todo", "title": "할 일 관리 앱 리팩터링", "desc": "클래스 컴포넌트를 함수 컴포넌트와 커스텀 훅으로 바꾸고 테스트를 추가했습니다."}
  ],
  "awards": [],
  "introduction": "디자인 전공을 하다 개발을 시작했습니다. 화면을 만드는 사람이 사용자에게 가장 가까이 있다고 생각합니다."
}$j$),
('jihoon-han', '커머스 3년차 백엔드 개발자', 'BACKEND', 120, $j${
  "basic": {"name": "한지훈", "school": "숭실대학교 소프트웨어학부 졸업", "github": "github.com/jihoon-han"},
  "skills": ["Java", "Spring Boot", "JPA", "MySQL", "Redis", "Kafka", "AWS"],
  "projects": [{"id": "p-jihoon-order", "title": "주문 조회 API 성능 개선", "desc": "응답 시간 1.2초 → 180ms"}],
  "awards": [],
  "introduction": "신입 때 받은 리뷰가 큰 도움이 되어 지금은 리뷰를 돌려주고 있습니다."
}$j$),
('yerin-song', '4년차 프론트엔드 개발자 송예린', 'FRONTEND', 150, $j${
  "basic": {"name": "송예린", "school": "아주대학교 미디어학과 졸업", "github": "github.com/yerin-song"},
  "skills": ["TypeScript", "React", "Next.js", "Emotion", "Web Performance"],
  "projects": [{"id": "p-yerin-ds", "title": "데일리노트 디자인 시스템", "desc": "컴포넌트 60여 개, 사내 4개 서비스 도입"}],
  "awards": [],
  "introduction": "성능과 접근성을 함께 챙기는 화면을 만듭니다."
}$j$),
('taeyang-lim', '사이드 프로젝트를 서비스로 키우는 풀스택 개발자', 'FULLSTACK', 6, $j${
  "basic": {"name": "임태양", "school": "부경대학교 컴퓨터공학과 졸업", "github": "github.com/taeyang-lim"},
  "skills": ["TypeScript", "React", "NestJS", "Prisma", "PostgreSQL", "Docker"],
  "projects": [
    {"id": "p-taeyang-camp", "title": "캠핑장 빈자리 알림 서비스", "desc": "공공 캠핑장 예약 사이트를 5분마다 확인해 빈자리가 나면 카카오 알림톡으로 알려 줍니다. 월 사용자 2,100명, 알림 발송 3만 건."},
    {"id": "p-taeyang-blog", "title": "개발 블로그 플랫폼", "desc": "마크다운 에디터와 코드 하이라이팅, 시리즈 기능을 직접 구현했습니다."}
  ],
  "awards": [],
  "introduction": "작게 만들어 빨리 내보내고, 쓰는 사람의 반응을 보며 고쳐 나가는 걸 좋아합니다."
}$j$),
('seungwoo-oh', '기본기를 다지는 백엔드 개발자 지망생', 'BACKEND', 5, $j${
  "basic": {"name": "오승우", "school": "단국대학교 소프트웨어학과 4학년", "github": "github.com/seungwoo-oh"},
  "skills": ["Java", "Spring Boot", "MySQL", "JUnit"],
  "projects": [
    {"id": "p-seungwoo-library", "title": "도서관 좌석 예약 시스템", "desc": "Spring Boot로 좌석 예약·연장·반납 API를 만들었습니다. 테스트 코드 커버리지 85%."}
  ],
  "awards": [],
  "introduction": "모르는 것을 정리하며 공부하는 습관이 있습니다. 블로그에 글 70개를 썼습니다."
}$j$),
('gaeun-baek', '기록하며 성장하는 프론트엔드 개발자', 'FRONTEND', 18, $j${
  "basic": {"name": "백가은", "school": "이화여자대학교 경영학과 졸업", "github": "github.com/gaeun-baek"},
  "skills": ["JavaScript", "TypeScript", "React", "Next.js", "Sass"],
  "projects": [
    {"id": "p-gaeun-budget", "title": "가계부 대시보드", "desc": "은행 거래 내역 CSV를 올리면 카테고리별 지출을 차트로 보여 줍니다. 경영학 전공 지식을 살려 예산 대비 지출 분석 기능을 넣었습니다."},
    {"id": "p-gaeun-portfolio", "title": "개인 포트폴리오 사이트", "desc": "Next.js 정적 생성과 이미지 최적화로 Lighthouse 성능 98점."}
  ],
  "awards": [{"title": "구글 애널리틱스 자격증", "date": "2025.08"}],
  "introduction": "비전공자로 시작해 매일 배운 것을 블로그에 기록하고 있습니다. 숫자로 사용자를 이해하는 프론트엔드 개발자가 목표입니다."
}$j$)
) AS r(username, title, field, days, content)
JOIN u ON u.name = r.username;

-- 프로필에 대표 이력서로 고정
UPDATE users SET pinned_common_resume_id = c.id
FROM common_resumes c
WHERE c.user_id = users.id AND users.email LIKE '%@demo.resuit.kr';

-- 5. 공고와 자기소개 문항
INSERT INTO job (company_id, type, title, location, employment_type, career, description, requirements, preferred, deadline, status, created_at, updated_at)
SELECT u.id, j.type, j.title, j.location, j.emp, j.career, j.descr, j.req, j.pref,
       CURRENT_DATE + j.deadline_days, j.status,
       LOCALTIMESTAMP - make_interval(days => j.posted_days), LOCALTIMESTAMP - make_interval(days => j.posted_days)
FROM (VALUES
('finroad', 'SPECIAL', '백엔드 신입 개발자 (Java/Spring)', '서울 강남구', '정규직', '신입',
 '핀로드의 간편 송금 서비스는 하루 80만 건의 이체를 처리합니다. 송금·계좌 연동 API를 함께 만들고 운영할 백엔드 신입 개발자를 찾습니다. 입사 후 3개월 동안 시니어 개발자와 1:1 온보딩을 진행합니다.',
 E'- Java와 Spring Boot로 API를 만들어 본 경험\n- RDB와 트랜잭션에 대한 이해\n- 2027년 2월 이전 졸업(예정)자',
 E'- 동시성 문제를 해결해 본 경험\n- 테스트 코드 작성 습관\n- 금융 도메인에 대한 관심', 21, 'OPEN', 9),
('finroad', 'GENERAL', '프론트엔드 개발자 (React, 1~3년)', '서울 강남구', '정규직', '경력 1~3년',
 '자산 관리 웹 서비스의 대시보드와 차트 화면을 개발합니다.',
 E'- React와 TypeScript 실무 경험 1년 이상\n- 상태 관리와 서버 상태 캐싱 이해',
 E'- 차트 라이브러리 사용 경험\n- 웹 접근성 개선 경험', 30, 'OPEN', 12),
('moakitchen', 'SPECIAL', '프론트엔드 신입 개발자', '서울 성동구', '정규직', '신입',
 '동네 반찬가게 주문 앱의 웹 화면을 함께 만들 프론트엔드 신입 개발자를 모십니다. 디자이너 2명, 프론트엔드 4명이 한 팀으로 일합니다.',
 E'- React로 서비스를 만들어 본 경험\n- HTML·CSS 기본기',
 E'- 디자인 시스템이나 컴포넌트 문서화 경험\n- 사용자 인터뷰나 데이터로 화면을 개선해 본 경험', 14, 'OPEN', 7),
('bridgelabs', 'GENERAL', '풀스택 개발자 (Next.js/Spring)', '경기 성남시 판교', '정규직', '경력 1년 이상',
 '제조사 주문·재고 관리 SaaS의 기능을 화면부터 API까지 맡아 개발합니다.',
 E'- Next.js 또는 React 실무 경험\n- Spring Boot API 개발 경험',
 E'- B2B SaaS 경험\n- CI/CD 구축 경험', 40, 'OPEN', 15),
('bridgelabs', 'SPECIAL', '백엔드 개발자 (신입~2년)', '경기 성남시 판교', '정규직', '신입~경력 2년',
 '하루 수십만 건의 재고 변동을 처리하는 주문·재고 API를 개발합니다.',
 E'- Java/Kotlin, Spring 기반 개발 경험\n- SQL 작성과 인덱스 이해',
 E'- 대용량 데이터 처리 경험\n- 검색 엔진 사용 경험', 10, 'OPEN', 18),
('orbit-mobility', 'SPECIAL', '서버 개발자 (Kotlin/Spring, 1~3년)', '서울 마포구', '정규직', '경력 1~3년',
 '킥보드·자전거 대여와 결제 서버를 개발합니다.',
 E'- Kotlin 또는 Java 실무 경험 1년 이상\n- 결제나 정산 도메인 경험',
 E'- Kafka 등 메시지 큐 운영 경험', -5, 'CLOSED', 40)
) AS j(company, type, title, location, emp, career, descr, req, pref, deadline_days, status, posted_days)
JOIN u ON u.name = j.company;

CREATE TEMP TABLE jb ON COMMIT DROP AS
    SELECT j.id, j.title, j.type, c.username AS company, c.nickname AS company_name
    FROM job j JOIN users c ON c.id = j.company_id
    WHERE c.email LIKE '%@demo.resuit.kr';

INSERT INTO job_questions (job_id, content, char_limit, order_no)
SELECT jb.id, q.content, q.char_limit, q.ord
FROM (VALUES
    ('finroad', '백엔드 신입 개발자 (Java/Spring)', '핀로드에 지원한 동기와 입사 후 가장 먼저 해 보고 싶은 일을 적어 주세요.', 700, 1),
    ('finroad', '백엔드 신입 개발자 (Java/Spring)', '가장 어려웠던 기술 문제와 그것을 해결한 과정을 구체적으로 적어 주세요.', 1000, 2),
    ('moakitchen', '프론트엔드 신입 개발자', '사용자 경험을 개선했던 경험을 하나 골라, 개선 전후를 비교해 적어 주세요.', 800, 1),
    ('moakitchen', '프론트엔드 신입 개발자', '모아키친 앱을 써 보고 가장 먼저 고치고 싶은 화면과 이유를 적어 주세요.', 600, 2),
    ('bridgelabs', '백엔드 개발자 (신입~2년)', '협업 중 의견이 갈렸던 경험과 어떻게 합의했는지 적어 주세요.', 700, 1),
    ('bridgelabs', '백엔드 개발자 (신입~2년)', '데이터가 많아져 성능 문제가 생겼던 경험과 해결 방법을 적어 주세요.', 1000, 2),
    ('orbit-mobility', '서버 개발자 (Kotlin/Spring, 1~3년)', '운영 중 장애를 겪었던 경험과 재발을 막기 위해 한 일을 적어 주세요.', 1000, 1)
) AS q(company, job_title, content, char_limit, ord)
JOIN jb ON jb.company = q.company AND jb.title = q.job_title;

-- 6. 특화 이력서: 공통 이력서 내용을 가져와 문항 답변을 붙인다
CREATE TEMP TABLE tr_src ON COMMIT DROP AS
SELECT * FROM (VALUES
('jiwon-kim', 'finroad', '백엔드 신입 개발자 (Java/Spring)', '핀로드 백엔드 신입 지원 - 김지원', 4,
 '매달 부모님께 용돈을 보내며 송금 앱을 쓰다가, 이체 버튼 한 번 뒤에 얼마나 많은 검증이 숨어 있을지 궁금해졌습니다. 좌석 예매 프로젝트에서 중복 예매를 막으며 "한 번만 일어나야 하는 일"을 보장하는 것이 얼마나 어려운지 배웠고, 이 경험을 송금처럼 더 무거운 도메인에서 키우고 싶어 지원했습니다. 입사 후에는 이체 API의 멱등성 처리 방식을 먼저 깊이 익히고, 장애 대응 문서를 읽으며 운영 감각을 기르고 싶습니다.',
 '좌석 예매 서비스 오픈 테스트에서 같은 좌석이 두 명에게 팔리는 문제가 생겼습니다. 처음에는 DB 비관적 락을 걸었지만 1분에 3,000건이 몰리자 커넥션이 부족해 응답이 5초를 넘었습니다. 낙관적 락, 비관적 락, Redis 분산 락을 같은 조건에서 nGrinder로 비교했고, 좌석 단위 Redis 락과 대기열을 함께 쓰는 방식을 골랐습니다. 락 획득 실패 시 재시도 횟수를 제한하고 사용자에게 대기 순번을 보여 주도록 바꿨습니다. 그 결과 중복 예매는 0건이 되었고 평균 응답 시간은 210ms로 안정되었습니다. 비교 결과를 팀 위키에 정리해 다음 기수가 그대로 활용하고 있습니다.'),
('jiwon-kim', 'bridgelabs', '백엔드 개발자 (신입~2년)', '브릿지랩스 백엔드 지원 - 김지원', 9,
 '좌석 예매 프로젝트에서 락 방식을 두고 팀원과 의견이 갈렸습니다. 저는 Redis 락을, 팀원은 DB 락을 주장했습니다. 말로 설득하는 대신 같은 시나리오로 부하 테스트를 돌려 결과를 표로 비교하자고 제안했고, 숫자를 본 뒤 함께 결정했습니다.',
 '중고거래 앱의 게시글 목록 API가 사용자가 늘면서 2초 넘게 걸렸습니다. 로그를 보니 한 번 조회에 쿼리가 41개 나가고 있었고, fetch join과 배치 사이즈 설정으로 3개까지 줄여 응답을 300ms 안으로 만들었습니다.'),
('yunho-choi', 'finroad', '백엔드 신입 개발자 (Java/Spring)', '핀로드 지원 이력서 - 최윤호', 6,
 '정산 배치를 다루며 금융 데이터의 정확성이 얼마나 중요한지 배웠습니다. 핀로드에서 송금 도메인으로 경험을 넓히고 싶습니다.',
 '정산 배치가 40분 넘게 걸려 아침 정산 마감을 넘기는 일이 잦았습니다. 청크 크기를 조정하고 가맹점 단위로 파티셔닝해 병렬 처리했고, 처리 시간을 7분으로 줄였습니다.'),
('minjae-kang', 'finroad', '백엔드 신입 개발자 (Java/Spring)', '핀로드 백엔드 신입 - 강민재', 5,
 '느린 화면의 원인을 찾아 고치는 일을 좋아합니다. 이체처럼 빠르고 정확해야 하는 서비스에서 그 강점을 쓰고 싶습니다.',
 '강의평 검색이 LIKE 검색으로 2.4초씩 걸렸습니다. Elasticsearch로 옮기고 형태소 분석기를 적용해 90ms로 줄였고, 동기화 지연을 줄이려 변경 이벤트를 큐로 보냈습니다.'),
('seungwoo-oh', 'finroad', '백엔드 신입 개발자 (Java/Spring)', '핀로드 지원 - 오승우', 2,
 '금융 서비스의 안정성에 관심이 많아 지원했습니다. 입사 후에는 테스트 코드 문화를 먼저 익히고 싶습니다.',
 '좌석 예약 시스템에서 연장 요청이 동시에 들어오면 시간이 두 번 늘어나는 문제가 있었습니다. 낙관적 락으로 해결했습니다.'),
('harin-lee', 'moakitchen', '프론트엔드 신입 개발자', '모아키친 프론트엔드 신입 - 이하린', 5,
 '레시피 앱의 첫 화면이 3초 넘게 걸려 이탈이 많았습니다. 이미지 지연 로딩과 가상 스크롤을 적용해 1.4초로 줄였고, 이탈률이 42%에서 27%로 내려갔습니다.',
 '반찬 상세 화면에서 옵션 선택과 수량 변경이 다른 영역에 있어 주문까지 손이 많이 갑니다. 하단 고정 영역에 옵션과 수량을 모으면 주문 완료율이 오를 거라 생각합니다.'),
('soah-yoon', 'moakitchen', '프론트엔드 신입 개발자', '모아키친 지원 이력서 - 윤소아', 3,
 '산책 메이트 서비스에서 지도와 목록을 오가며 사람을 찾기 어렵다는 피드백을 받았습니다. 지도 위에 카드 목록을 겹쳐 보여 주도록 바꿨더니 매칭 요청이 두 배로 늘었습니다.',
 '가게 목록에서 영업 종료된 가게가 섞여 보여 헛걸음하게 됩니다. 영업 상태로 정렬하고 종료 가게는 흐리게 보여 주고 싶습니다.'),
('gaeun-baek', 'moakitchen', '프론트엔드 신입 개발자', '모아키친 프론트엔드 지원 - 백가은', 7,
 '가계부 대시보드 사용자 인터뷰에서 차트가 많아 핵심을 놓친다는 말을 들었습니다. 이번 달 예산 대비 지출 한 줄 요약을 맨 위에 두자 재방문율이 올랐습니다.',
 '장바구니에서 가게별로 배달비가 따로 붙는 것을 결제 직전에야 알 수 있습니다. 담는 순간 배달비를 보여 주고 싶습니다.'),
('minjae-kang', 'bridgelabs', '백엔드 개발자 (신입~2년)', '브릿지랩스 백엔드 지원 - 강민재', 4,
 '팀 프로젝트에서 ORM과 SQL 직접 작성을 두고 의견이 갈렸습니다. 조회가 복잡한 화면만 QueryDSL을 쓰기로 기준을 정해 합의했습니다.',
 '게시판 깊은 페이지 조회가 6초 넘게 걸렸습니다. 실행 계획을 보고 복합 인덱스를 추가하고 커서 기반 페이지네이션으로 바꿔 200ms로 줄였습니다.'),
('yunho-choi', 'orbit-mobility', '서버 개발자 (Kotlin/Spring, 1~3년)', '오르빗모빌리티 서버 개발자 지원 - 최윤호', 30,
 '카드사 응답 지연으로 환불이 중복 요청되는 장애가 있었습니다. 멱등키를 도입하고 타임아웃 알림을 Datadog에 추가해 같은 장애가 다시 나지 않게 했습니다.', NULL)
) AS t(username, company, job_title, title, days, answer1, answer2);

INSERT INTO target_resumes (job_id, user_id, common_resume_id, title, content, created_at, updated_at)
SELECT jb.id, u.id, c.id, t.title,
       c.content || jsonb_build_object('answers', (
           SELECT COALESCE(jsonb_agg(jsonb_build_object('questionId', q.id,
                       'answer', CASE q.order_no WHEN 1 THEN t.answer1 ELSE t.answer2 END) ORDER BY q.order_no), '[]'::jsonb)
           FROM job_questions q WHERE q.job_id = jb.id)),
       LOCALTIMESTAMP - make_interval(days => t.days), LOCALTIMESTAMP - make_interval(days => t.days) + INTERVAL '5 hours'
FROM tr_src t
JOIN u ON u.name = t.username
JOIN jb ON jb.company = t.company AND jb.title = t.job_title
JOIN common_resumes c ON c.user_id = u.id;

-- 7. 지원 (제출 당시 이력서를 스냅샷으로 남긴다)
-- 특화 공고: 김지원(브릿지랩스), 최윤호(핀로드·오르빗), 강민재(핀로드), 이하린·백가은(모아키친)
-- 김지원의 핀로드, 오승우, 윤소아, 강민재의 브릿지랩스 이력서는 아직 작성 중이다.
INSERT INTO job_applications (job_id, user_id, target_resume_id, submitted_resume_type, submitted_company_name, submitted_resume_title,
                              submitted_resume_snapshot, snapshot_expires_at, status, applied_at)
SELECT jb.id, u.id, tr.id, 'TARGET', jb.company_name, tr.title,
       jsonb_build_object('schemaVersion', 1, 'title', tr.title, 'field', NULL, 'content', tr.content,
           'questions', (SELECT COALESCE(jsonb_agg(jsonb_build_object('id', q.id, 'content', q.content, 'charLimit', q.char_limit, 'orderNo', q.order_no) ORDER BY q.order_no), '[]'::jsonb)
                         FROM job_questions q WHERE q.job_id = jb.id)),
       LOCALTIMESTAMP - make_interval(days => a.days) + INTERVAL '3 months', 'APPLIED',
       LOCALTIMESTAMP - make_interval(days => a.days)
FROM (VALUES
    ('jiwon-kim', 'bridgelabs', '백엔드 개발자 (신입~2년)', 7),
    ('yunho-choi', 'finroad', '백엔드 신입 개발자 (Java/Spring)', 4),
    ('minjae-kang', 'finroad', '백엔드 신입 개발자 (Java/Spring)', 2),
    ('harin-lee', 'moakitchen', '프론트엔드 신입 개발자', 3),
    ('gaeun-baek', 'moakitchen', '프론트엔드 신입 개발자', 1),
    ('yunho-choi', 'orbit-mobility', '서버 개발자 (Kotlin/Spring, 1~3년)', 28)
) AS a(username, company, job_title, days)
JOIN u ON u.name = a.username
JOIN jb ON jb.company = a.company AND jb.title = a.job_title
JOIN target_resumes tr ON tr.user_id = u.id AND tr.job_id = jb.id;

-- 일반 공고: 공통 이력서로 지원
INSERT INTO job_applications (job_id, user_id, common_resume_id, submitted_resume_type, submitted_company_name, submitted_resume_title,
                              submitted_resume_snapshot, snapshot_expires_at, status, applied_at)
SELECT jb.id, u.id, c.id, 'COMMON', jb.company_name, c.title,
       jsonb_build_object('schemaVersion', 1, 'title', c.title, 'field', c.field, 'content', c.content, 'questions', '[]'::jsonb),
       LOCALTIMESTAMP - make_interval(days => a.days) + INTERVAL '3 months', 'APPLIED',
       LOCALTIMESTAMP - make_interval(days => a.days)
FROM (VALUES
    ('seojun.park', 'finroad', '프론트엔드 개발자 (React, 1~3년)', 5),
    ('daeun-jeong', 'bridgelabs', '풀스택 개발자 (Next.js/Spring)', 6),
    ('taeyang-lim', 'bridgelabs', '풀스택 개발자 (Next.js/Spring)', 2)
) AS a(username, company, job_title, days)
JOIN u ON u.name = a.username
JOIN jb ON jb.company = a.company AND jb.title = a.job_title
JOIN common_resumes c ON c.user_id = u.id;

-- 8. 피드백 게시판 요청 (특화 이력서는 회사명이 가려져 보인다)
INSERT INTO feedback_requests (user_id, common_resume_id, target_resume_id, content, category, created_at, updated_at)
SELECT u.id,
       CASE WHEN r.company IS NULL THEN c.id END,
       CASE WHEN r.company IS NOT NULL THEN tr.id END,
       r.content, r.category,
       LOCALTIMESTAMP - make_interval(hours => r.hours), LOCALTIMESTAMP - make_interval(hours => r.hours)
FROM (VALUES
    ('jiwon-kim', 'finroad', 'BACKEND', 70,
     '핀테크 회사 백엔드 신입 공고에 낼 이력서예요. 좌석 예매 프로젝트의 동시성 처리 과정이 면접관에게 잘 전달되는지, 두 번째 문항 답변이 너무 장황하지 않은지 봐 주세요!'),
    ('soah-yoon', 'moakitchen', 'FRONTEND', 52,
     '첫 지원이라 많이 떨려요. 부트캠프 팀 프로젝트를 어떻게 하면 제 기여가 잘 보이게 쓸 수 있을지 조언 부탁드립니다.'),
    ('seojun.park', NULL, 'FRONTEND', 150,
     '1~3년차 프론트엔드 포지션에 쓰려는 공통 이력서입니다. 기술 스택이 너무 나열식인지, 프로젝트 성과가 잘 드러나는지 궁금해요.'),
    ('minjae-kang', 'bridgelabs', 'BACKEND', 96,
     '성능 개선 경험을 문항 답변에 녹였는데 숫자가 너무 많아서 읽기 힘들지 않을까 걱정입니다.'),
    ('daeun-jeong', NULL, 'FULLSTACK', 30,
     '혼자 만든 서비스를 어떻게 소개해야 할지 고민이에요. 자기소개가 너무 짧은 것 같기도 하고요.'),
    ('seungwoo-oh', 'finroad', 'BACKEND', 20,
     '처음 써 보는 이력서입니다. 프로젝트가 하나뿐이라 부족해 보이는데, 어떤 부분을 보완하면 좋을까요?'),
    ('gaeun-baek', NULL, 'FRONTEND', 220,
     '비전공자라 경영학 전공을 강점으로 쓰고 싶은데 어떻게 녹이면 좋을지 모르겠어요. 전체적으로 봐 주세요.'),
    ('taeyang-lim', NULL, 'FULLSTACK', 2,
     '캠핑장 알림 서비스를 대표 프로젝트로 넣었는데, 기술적인 고민이 잘 안 보이는 것 같아요.')
) AS r(username, company, category, hours, content)
JOIN u ON u.name = r.username
JOIN common_resumes c ON c.user_id = u.id
LEFT JOIN jb ON jb.company = r.company AND jb.type = 'SPECIAL'
LEFT JOIN target_resumes tr ON tr.user_id = u.id AND tr.job_id = jb.id;

CREATE TEMP TABLE fr ON COMMIT DROP AS
    SELECT r.id, u.name AS requester, r.created_at
    FROM feedback_requests r JOIN u ON u.id = r.user_id;

INSERT INTO feedback_request_sections (request_id, section_type)
SELECT fr.id, s.section
FROM (VALUES
    ('jiwon-kim', 'PROJECTS'), ('jiwon-kim', 'ANSWERS'),
    ('soah-yoon', 'PROJECTS'), ('soah-yoon', 'INTRODUCTION'),
    ('seojun.park', 'SKILLS'), ('seojun.park', 'PROJECTS'),
    ('minjae-kang', 'ANSWERS'), ('minjae-kang', 'PROJECTS'),
    ('daeun-jeong', 'INTRODUCTION'), ('daeun-jeong', 'PROJECTS'),
    ('seungwoo-oh', 'BASIC'), ('seungwoo-oh', 'SKILLS'), ('seungwoo-oh', 'PROJECTS'),
    ('taeyang-lim', 'PROJECTS')
) AS s(requester, section)
JOIN fr ON fr.requester = s.requester;

-- 9. 피드백 댓글 (김지원의 요청은 아직 채택 전이라 시연에서 채택할 수 있다)
INSERT INTO feedback_comments (feedback_id, user_id, section, content, is_adopted, adopted_at, created_at, updated_at)
SELECT fr.id, u.id, c.section, c.content, c.adopted,
       CASE WHEN c.adopted THEN fr.created_at + make_interval(hours => c.after_hours + 6) END,
       fr.created_at + make_interval(hours => c.after_hours), fr.created_at + make_interval(hours => c.after_hours)
FROM (VALUES
    ('jiwon-kim', 'jihoon-han', 'PROJECTS', false, 3,
     E'락 방식 세 가지를 비교한 과정이 정말 좋습니다. 다만 지금은 결과(중복 0건, 210ms)가 문장 끝에 묻혀 있어요. 첫 문장을 "1분 3,000건 요청에서 중복 예매 0건"처럼 결과로 시작하고, 왜 Redis 락을 골랐는지 한 줄을 덧붙이면 면접에서 바로 질문이 들어올 거예요.'),
    ('jiwon-kim', 'yunho-choi', 'ANSWERS', false, 9,
     E'2번 답변이 1,000자 제한에 거의 꽉 차 있어요. "처음에는 비관적 락을 걸었지만" 부분을 한 문장으로 줄이고, 마지막 위키 정리 이야기는 협업 문항이 따로 없으니 남겨 두는 게 좋겠습니다.'),
    ('jiwon-kim', 'minjae-kang', 'PROJECTS', false, 20,
     E'중고거래 앱에서 쿼리 41개 → 3개는 숫자가 아주 좋네요! 응답 시간도 같이 적어 주면 효과가 더 잘 보일 것 같아요.'),
    ('soah-yoon', 'yerin-song', 'PROJECTS', true, 4,
     E'팀 프로젝트는 "내가 맡은 부분"과 "그래서 바뀐 것"을 따로 적어 보세요. 지도·채팅 화면을 맡았다면 지도 위 카드 목록 개선으로 매칭 요청이 두 배가 된 이야기를 프로젝트 설명 쪽으로 옮기는 게 훨씬 강해요.'),
    ('soah-yoon', 'harin-lee', 'INTRODUCTION', false, 10,
     E'디자인 전공 이야기가 좋아요. 디자인을 공부해서 개발에 어떤 도움이 됐는지 사례 하나만 더 붙이면 좋겠어요.'),
    ('soah-yoon', 'gaeun-baek', NULL, false, 26,
     E'저도 비슷한 상황이었는데 깃허브 README에 화면 GIF를 넣었더니 면접에서 많이 물어보셨어요. 같이 힘내요!'),
    ('seojun.park', 'yerin-song', 'SKILLS', true, 5,
     E'기술 스택을 나열만 하면 모두 비슷해 보여요. React Query처럼 프로젝트에서 실제로 문제를 해결한 기술은 프로젝트 설명에 "왜 썼는지"를 함께 적어 주세요. Storybook과 Jest는 어느 정도 썼는지 범위가 보이면 좋겠습니다.'),
    ('seojun.park', 'taeyang-lim', 'PROJECTS', false, 30,
     E'접근성 점수 68 → 94 성과가 인상적이에요. 1~3년차 공고라면 이걸 첫 번째 프로젝트로 올리는 것도 방법일 것 같아요.'),
    ('minjae-kang', 'jihoon-han', 'ANSWERS', true, 6,
     E'숫자가 많은 건 장점이에요. 대신 한 문단에 숫자를 두 개까지만 두고, 나머지는 "6초 → 200ms"처럼 화살표 표기로 정리하면 읽기 편합니다. 실행 계획에서 무엇을 봤는지(풀 스캔이었는지 등) 한 줄 추가를 추천해요.'),
    ('minjae-kang', 'seungwoo-oh', 'PROJECTS', false, 40,
     E'커서 기반 페이지네이션 적용기 너무 잘 봤습니다. 혹시 블로그에 정리하신 글이 있으면 링크도 넣어 주시면 좋을 것 같아요.'),
    ('daeun-jeong', 'jihoon-han', 'PROJECTS', false, 8,
     E'사용자 380명은 혼자 만든 서비스로 충분히 강한 숫자예요. 운영하면서 생긴 문제(알림 중복, 장애 등)와 해결을 하나만 넣으면 풀스택으로서의 깊이가 보일 거예요.'),
    ('daeun-jeong', 'yerin-song', 'INTRODUCTION', false, 12,
     E'"다음 날 고쳐서 배포"하는 주기를 좋아한다는 게 매력적인데, 실제로 그렇게 고친 사례 하나를 붙이면 훨씬 설득력이 생겨요.'),
    ('seungwoo-oh', 'yunho-choi', 'PROJECTS', false, 6,
     E'프로젝트가 하나여도 괜찮아요. 연장 요청이 두 번 처리되던 문제를 낙관적 락으로 해결한 이야기를 프로젝트 설명에도 넣어 주세요. 테스트 커버리지 85%는 어떤 테스트를 썼는지 함께 적으면 좋습니다.'),
    ('gaeun-baek', 'yerin-song', NULL, true, 7,
     E'경영학 전공은 "숫자로 사용자를 이해한다"는 문장과 아주 잘 맞아요. 가계부 대시보드에서 예산 대비 지출 분석을 넣은 이유를 전공과 연결해서 쓰면 비전공이 약점이 아니라 강점으로 보입니다.'),
    ('gaeun-baek', 'soah-yoon', 'PROJECTS', false, 30,
     E'포트폴리오 사이트 Lighthouse 98점 대단해요! 어떤 최적화를 했는지도 궁금해요.')
) AS c(requester, author, section, adopted, after_hours, content)
JOIN fr ON fr.requester = c.requester
JOIN u ON u.name = c.author;

-- 답글 (요청자가 남긴 감사 인사)
INSERT INTO feedback_comments (feedback_id, parent_id, user_id, section, content, is_adopted, created_at, updated_at)
SELECT fr.id, p.comment_id, req.id, p.section, r.content, false,
       p.created_at + INTERVAL '2 hours', p.created_at + INTERVAL '2 hours'
FROM (VALUES
    ('jiwon-kim', 'jihoon-han', '결과로 시작하라는 말씀 정말 와닿네요. 첫 문장부터 바꿔 보겠습니다. 감사합니다!'),
    ('soah-yoon', 'yerin-song', '프로젝트 설명으로 옮기니까 훨씬 눈에 들어와요. 바로 고쳤어요!'),
    ('minjae-kang', 'jihoon-han', '풀 스캔이었던 부분 추가했습니다. 꼼꼼히 봐 주셔서 감사해요.')
) AS r(requester, author, content)
JOIN fr ON fr.requester = r.requester
JOIN u req ON req.name = r.requester
JOIN u a ON a.name = r.author
JOIN feedback_comments p ON p.feedback_id = fr.id AND p.user_id = a.id AND p.parent_id IS NULL;

-- 도움돼요(좋아요)
INSERT INTO feedback_comment_likes (comment_id, user_id, created_at)
SELECT c.comment_id, liker.id, c.created_at + INTERVAL '1 hour'
FROM (VALUES
    ('jiwon-kim', 'jihoon-han', 'seungwoo-oh'), ('jiwon-kim', 'jihoon-han', 'minjae-kang'), ('jiwon-kim', 'jihoon-han', 'yunho-choi'),
    ('soah-yoon', 'yerin-song', 'harin-lee'), ('soah-yoon', 'yerin-song', 'gaeun-baek'),
    ('seojun.park', 'yerin-song', 'harin-lee'), ('seojun.park', 'yerin-song', 'taeyang-lim'),
    ('minjae-kang', 'jihoon-han', 'jiwon-kim'), ('daeun-jeong', 'jihoon-han', 'taeyang-lim'),
    ('gaeun-baek', 'yerin-song', 'soah-yoon'), ('gaeun-baek', 'yerin-song', 'harin-lee')
) AS l(requester, author, liker)
JOIN fr ON fr.requester = l.requester
JOIN u a ON a.name = l.author
JOIN u liker ON liker.name = l.liker
JOIN feedback_comments c ON c.feedback_id = fr.id AND c.user_id = a.id AND c.parent_id IS NULL;

-- 10. 피드 글 (일반·기술 정보·질문). 최근 2주에 걸쳐 흩어 놓는다
CREATE TEMP TABLE demo_posts (
    k       int PRIMARY KEY,
    author  varchar(50) NOT NULL,
    type    varchar(20) NOT NULL,
    hours   int NOT NULL,
    likes   int NOT NULL,
    tech    text[] NOT NULL,
    tags    text[] NOT NULL,
    content text NOT NULL,
    repo    varchar(200)
) ON COMMIT DROP;

INSERT INTO demo_posts VALUES
(1, 'jihoon-han', 'TECH', 5, 11, '{Spring,Java}', '{JPA,성능개선}',
E'주문 조회 API가 1.2초 걸리던 원인, 결국 N+1이 아니라 OSIV였습니다.\n\n컨트롤러에서 DTO로 바꾸는 동안 지연 로딩이 계속 터지고 있었는데, 로그에는 쿼리가 몇 개 안 보여서 한참 헤맸어요. spring.jpa.open-in-view=false로 끄고 서비스 안에서 fetch join으로 필요한 것만 읽게 바꿨더니 180ms까지 내려갔습니다.\n\n신입분들 프로젝트 리뷰하다 보면 OSIV 켜진 채로 두는 경우가 정말 많아요. 한 번씩 확인해 보세요.'),
(2, 'jiwon-kim', 'GENERAL', 9, 8, '{}', '{취업준비,회고}',
E'이력서 피드백 게시판에 처음 올려 봤는데 반나절 만에 현직자분 리뷰가 달렸다. "결과로 첫 문장을 시작하라"는 한 줄이 제일 크게 와닿았다.\n\n프로젝트 설명을 전부 다시 쓰는 중. 숫자는 있었는데 문장 맨 끝에 숨어 있었네.'),
(3, 'yerin-song', 'TECH', 20, 14, '{React,TypeScript}', '{프론트엔드,성능}',
E'React 리스트 렌더링이 느릴 때 제가 순서대로 확인하는 것들\n\n1. key가 index는 아닌지\n2. 부모 상태가 바뀔 때 리스트 전체가 다시 그려지는지 (React DevTools Profiler)\n3. 아이템이 200개가 넘으면 가상 스크롤\n4. 이미지 lazy loading과 width/height 지정\n\n대부분 1, 2에서 끝납니다. memo부터 붙이지 마세요!'),
(4, 'soah-yoon', 'QUESTION', 26, 3, '{}', '{신입,포트폴리오}',
E'부트캠프 팀 프로젝트를 포트폴리오에 넣을 때, 제가 안 맡은 기능도 소개해야 할까요?\n\n지도·채팅 화면만 맡았는데 서비스 전체를 설명하자니 제 기여가 묻히는 것 같고, 제 부분만 쓰자니 서비스가 뭔지 안 보일 것 같아서 고민이에요.'),
(5, 'minjae-kang', 'TECH', 31, 9, '{Java,Spring}', '{MySQL,인덱스}',
E'OFFSET 페이지네이션이 뒤로 갈수록 느려지는 이유를 직접 재 봤습니다.\n\n게시글 50만 건 기준\n- 1페이지: 12ms\n- 1,000페이지: 480ms\n- 10,000페이지: 6.1초\n\nOFFSET은 앞의 행을 다 읽고 버리기 때문이에요. 마지막으로 본 id를 기준으로 읽는 커서 방식으로 바꾸니 어느 페이지든 15ms 안쪽이었습니다. 무한 스크롤이라면 커서 방식을 추천해요.'),
(6, 'finroad', 'GENERAL', 40, 12, '{}', '{채용,핀테크,신입개발자}',
E'핀로드에서 백엔드 신입 개발자를 모십니다.\n\n하루 80만 건의 이체를 처리하는 송금 서비스를 함께 만들 분을 찾고 있어요. 입사 후 3개월 동안 시니어 개발자와 1:1 온보딩을 진행합니다.\n\n동시성 문제를 직접 고민해 본 경험이 있다면 꼭 특화 이력서에 적어 주세요. 채용 공고 탭에서 확인하실 수 있습니다.'),
(7, 'taeyang-lim', 'GENERAL', 46, 10, '{}', '{사이드프로젝트,회고}',
E'캠핑장 빈자리 알림 서비스 월 사용자 2,000명 넘었습니다.\n\n시작은 제가 예약에 계속 실패해서 만든 크롤러였는데, 친구들한테 공유하다 보니 여기까지 왔네요. 이번 달 알림톡 비용이 서버비보다 많이 나오는 게 새로운 고민입니다.'),
(8, 'seojun.park', 'TECH', 52, 7, '{React}', '{접근성,a11y}',
E'스크린리더 사용자 인터뷰를 다섯 번 하고 배운 것\n\n- 아이콘 버튼에 aria-label이 없으면 그냥 "버튼"이라고만 읽힌다\n- 모달이 열려도 포커스가 뒤에 남아 있으면 사용자는 모달이 열린 줄 모른다\n- "여기를 클릭하세요" 링크는 목록으로 들으면 전부 같은 말이다\n\n접근성 점수 68 → 94. 체크리스트보다 실제 사용자 한 명의 이야기가 훨씬 강했어요.'),
(9, 'gaeun-baek', 'QUESTION', 60, 4, '{}', '{비전공,취업준비}',
E'비전공자분들, 면접에서 전공 질문 받으면 어떻게 답하셨나요?\n\n경영학과 나왔는데 "왜 개발로 왔냐"는 질문에 매번 답이 길어져요. 짧고 설득력 있게 말하는 방법이 궁금합니다.'),
(10, 'yunho-choi', 'TECH', 70, 9, '{Spring,Java}', '{SpringBatch,정산}',
E'Spring Batch 정산 배치 40분 → 7분으로 줄인 과정 정리\n\n1. 청크 크기 100 → 1,000 (커밋 횟수가 병목이었음)\n2. 가맹점 id 범위로 파티셔닝해서 8개 스레드로 병렬 처리\n3. ItemReader를 JPA에서 JdbcCursorItemReader로 교체\n\n가장 효과가 컸던 건 3번이었어요. 읽기만 하는 배치에서 영속성 컨텍스트는 생각보다 비쌉니다.'),
(11, 'harin-lee', 'GENERAL', 76, 6, '{}', '{디자인시스템,오픈소스}',
E'동아리 디자인 시스템 Pebble을 npm에 배포했어요!\n\n버튼, 입력, 모달 등 24개 컴포넌트를 Storybook으로 문서화했습니다. 벌써 동아리 프로젝트 세 팀이 쓰고 있어요. 피드백 언제든 환영합니다.'),
(12, 'daeun-jeong', 'QUESTION', 84, 5, '{}', '{배포,CI}',
E'개인 프로젝트 배포할 때 Vercel + 별도 백엔드 서버 조합 쓰시는 분들, CORS랑 쿠키 문제 어떻게 해결하셨나요?\n\n프론트는 Vercel, 백엔드는 EC2에 올렸더니 세션 쿠키가 안 붙어서 하루를 날렸어요.'),
(13, 'jihoon-han', 'GENERAL', 95, 15, '{}', '{이력서,신입}',
E'이번 주에 신입 이력서 10개 정도 리뷰하면서 공통으로 드린 말\n\n- 기술 스택 나열보다 "그 기술로 무슨 문제를 풀었는지"\n- 성과는 숫자로, 그리고 문장 맨 앞에\n- 팀 프로젝트는 내가 한 일과 그래서 바뀐 것을 분리해서\n\n다들 경험은 충분한데 쓰는 방식에서 손해를 보고 계세요.'),
(14, 'seungwoo-oh', 'QUESTION', 100, 2, '{}', '{Java,테스트}',
E'JUnit 테스트에서 @Transactional 붙이면 테스트 끝나고 롤백된다고 배웠는데, 실제 서비스 코드의 트랜잭션 동작이랑 달라질 수 있다는 글을 봤어요. 실무에서는 테스트에 @Transactional 쓰시나요?'),
(15, 'moakitchen', 'GENERAL', 110, 9, '{}', '{채용,프론트엔드}',
E'모아키친 프론트엔드 신입 채용을 시작했습니다.\n\n동네 반찬가게와 고객을 잇는 주문 앱을 디자이너 2명, 프론트엔드 4명이 한 팀으로 만들고 있어요. 사용자 인터뷰를 화면 개선으로 연결해 본 경험이 있다면 환영합니다.'),
(16, 'yerin-song', 'TECH', 125, 10, '{TypeScript}', '{타입스크립트}',
E'TypeScript에서 API 응답 타입을 믿으면 안 되는 이유\n\n서버가 null을 보낼 수 있는 필드를 string으로 선언해 두면 컴파일은 통과하고 런타임에 터집니다. 저희 팀은 응답 경계에서만 zod로 한 번 검증하고, 안쪽 코드는 타입을 믿도록 정했어요.'),
(17, 'jiwon-kim', 'TECH', 140, 6, '{Spring,Docker}', '{Redis,동시성}',
E'좌석 예매 동시성 처리, 락 세 가지를 같은 조건에서 비교해 봤습니다 (1분 3,000건)\n\n- 비관적 락: 정확하지만 커넥션 부족으로 응답 5초 이상\n- 낙관적 락: 충돌이 많아 재시도 폭주\n- Redis 분산 락 + 대기열: 중복 0건, 평균 210ms\n\n정답은 상황마다 다르지만, 직접 재 보기 전에는 몰랐던 숫자들이에요.'),
(18, 'bridgelabs', 'GENERAL', 150, 6, '{}', '{채용,B2B,SaaS}',
E'브릿지랩스는 중소 제조사의 주문·재고 관리를 돕는 SaaS를 만듭니다. 하루 수십만 건의 재고 변동을 다루는 백엔드 개발자와, 화면부터 API까지 함께 만들 풀스택 개발자를 찾고 있어요.'),
(19, 'minjae-kang', 'QUESTION', 165, 3, '{}', '{Elasticsearch}',
E'Elasticsearch로 한국어 검색할 때 nori 분석기 쓰시나요? 사용자 사전은 어떻게 관리하시는지 궁금합니다. 강의명에 들어간 약어가 자꾸 쪼개져서 검색이 안 돼요.'),
(20, 'taeyang-lim', 'TECH', 180, 7, '{Docker}', '{배포,인프라}',
E'월 1만 원짜리 서버 하나로 사이드 프로젝트 운영하는 구성\n\n- Docker Compose로 앱, DB, 리버스 프록시\n- 매일 새벽 DB 덤프를 오브젝트 스토리지에\n- 헬스체크 실패하면 디스코드 웹훅\n\n사용자 2,000명까지는 이걸로 충분했어요.'),
(21, 'harin-lee', 'QUESTION', 200, 2, '{}', '{CSS}',
E'디자인 토큰을 CSS 변수로 관리할 때 다크 모드는 변수 값을 통째로 바꾸시나요, 아니면 토큰을 따로 두시나요?'),
(22, 'yerin-song', 'GENERAL', 215, 12, '{}', '{회고,멘토링}',
E'신입 때 받은 이력서 리뷰 한 번이 커리어를 바꿨다고 생각해요. 그래서 요즘은 일주일에 두 개씩 피드백 게시판 글에 리뷰를 남기고 있습니다. 같이 하실 현직자분들 환영해요.'),
(23, 'yunho-choi', 'GENERAL', 240, 5, '{}', '{이직,회고}',
E'첫 회사 1년 2개월 회고\n\n정산 배치를 다시 만든 게 가장 큰 일이었고, 장애 대응하면서 멱등키의 중요성을 몸으로 배웠다. 다음 회사에서는 결제 앞단까지 다뤄 보고 싶다.'),
(24, 'soah-yoon', 'GENERAL', 260, 6, '{}', '{부트캠프,수료}',
E'6개월 부트캠프 수료했습니다. 디자인 전공하다 개발을 시작해서 처음엔 useEffect가 왜 두 번 도는지도 몰랐는데, 이제 팀 프로젝트 화면 하나를 맡아 끝까지 만들 수 있게 됐어요. 이제 진짜 취업 준비 시작!'),
(25, 'seungwoo-oh', 'TECH', 280, 4, '{Java}', '{자바,기본기}',
E'equals를 재정의하면 hashCode도 재정의해야 하는 이유를 HashSet으로 직접 확인해 봤습니다. equals만 재정의한 객체 두 개를 넣으면 같은 값인데도 두 개가 들어가요. 책으로 읽을 때보다 코드로 돌려 보니 확실히 기억에 남네요.'),
(26, 'daeun-jeong', 'TECH', 300, 6, '{React,AI}', '{Nextjs,개발생산성}',
E'Next.js 프로젝트에 AI 코드 리뷰를 붙여 봤어요. PR마다 자동으로 리뷰가 달리게 했더니 혼자 개발할 때 놓치던 에러 처리 누락을 꽤 잡아 줍니다. 다만 제안을 그대로 받기보다 "왜?"를 한 번 더 묻는 습관이 중요하더라고요.')
;

-- Resuit CLI(cli/)로 쓴 글. CLI가 보내는 그대로 넣는다.
--   resuit post / resuit code  → type NORMAL, resuit ask → QUESTION, resuit tech → TECH
--   resuit code 는 본문에 "제목 + 빈 줄 + ```언어 filename=파일명" 코드 블록을 넣고 repo_full_name 을 채운다.
--   ask/tech/post 는 제목 한 줄만 본문이 된다.
INSERT INTO demo_posts (k, author, type, hours, likes, tech, tags, content, repo) VALUES
(101, 'jiwon-kim', 'NORMAL', 2, 9, '{}', '{Redis,동시성,Spring}',
E'좌석 예매 Redis 분산 락 적용 코드 공유합니다\n\n```java filename=SeatLockService.java\n@Service\n@RequiredArgsConstructor\npublic class SeatLockService {\n    private final RedissonClient redisson;\n\n    public void reserve(Long seatId, Runnable task) {\n        RLock lock = redisson.getLock("seat:" + seatId);\n        try {\n            // 최대 3초 기다리고, 5초 뒤에는 자동으로 풀린다\n            if (!lock.tryLock(3, 5, TimeUnit.SECONDS)) {\n                throw new SeatBusyException(seatId);\n            }\n            task.run();\n        } catch (InterruptedException e) {\n            Thread.currentThread().interrupt();\n        } finally {\n            if (lock.isHeldByCurrentThread()) lock.unlock();\n        }\n    }\n}\n```',
 'jiwon-kim/concert-ticketing'),
(102, 'minjae-kang', 'NORMAL', 12, 7, '{}', '{MySQL,페이지네이션}',
E'커서 기반 페이지네이션 쿼리 (QueryDSL)\n\n```java filename=PostQueryRepository.java\npublic List<Post> findPage(Long cursorId, int size) {\n    return query.selectFrom(post)\n        .where(cursorId == null ? null : post.id.lt(cursorId))\n        .orderBy(post.id.desc())\n        .limit(size)\n        .fetch();\n}\n```',
 'minjae-kang/lecture-review'),
(103, 'taeyang-lim', 'NORMAL', 28, 6, '{}', '{Docker,배포}',
E'사이드 프로젝트 compose 헬스체크 설정\n\n```yml filename=compose.yml\nservices:\n  app:\n    image: camp-alert:latest\n    restart: unless-stopped\n    healthcheck:\n      test: ["CMD", "curl", "-f", "http://localhost:3000/health"]\n      interval: 30s\n      retries: 3\n```',
 'taeyang-lim/camp-alert'),
(104, 'yunho-choi', 'NORMAL', 55, 8, '{}', '{Kotlin,결제,멱등성}',
E'환불 API 멱등키 처리 (같은 요청이 두 번 와도 한 번만 환불)\n\n```kt filename=RefundService.kt\nfun refund(key: String, request: RefundRequest): RefundResult {\n    idempotencyRepository.findByKey(key)?.let { return it.result }\n    val result = paymentClient.refund(request)\n    idempotencyRepository.save(IdempotencyRecord(key, result))\n    return result\n}\n```',
 'yunho-choi/payment-lab'),
(105, 'seojun.park', 'NORMAL', 90, 5, '{}', '{React,접근성}',
E'모달 열릴 때 포커스 가두는 훅\n\n```tsx filename=useFocusTrap.tsx\nexport function useFocusTrap(ref: RefObject<HTMLElement>, open: boolean) {\n  useEffect(() => {\n    if (!open || !ref.current) return;\n    const first = ref.current.querySelector<HTMLElement>("button, [href], input");\n    first?.focus();\n  }, [open, ref]);\n}\n```',
 'seojun-park/a11y-kit'),
(106, 'daeun-jeong', 'NORMAL', 7, 6, '{}', '{CLI,개발생산성}',
 'IDE에서 커밋하고 터미널에서 resuit post로 바로 근황 올리는 중. 브라우저 안 켜도 돼서 편하다', NULL),
(107, 'harin-lee', 'TECH', 16, 5, '{React,TypeScript}', '{디자인시스템,Storybook}',
 'Storybook 8로 올리면서 컴포넌트 문서를 MDX에서 autodocs로 옮긴 후기', NULL),
(108, 'seungwoo-oh', 'QUESTION', 33, 2, '{}', '{Spring,JPA}',
 '@Transactional(readOnly = true)를 조회 메서드에 꼭 붙여야 하나요? 성능 차이가 체감될 만큼인지 궁금합니다', NULL),
(109, 'jihoon-han', 'TECH', 64, 8, '{Spring,Docker}', '{테스트,Testcontainers}',
 'H2 대신 Testcontainers로 통합 테스트를 돌렸더니 운영 DB에서만 나던 버그가 테스트에서 잡히기 시작했습니다', NULL);

INSERT INTO posts (user_id, type, content, repo_full_name, created_at, updated_at)
SELECT u.id, p.type, p.content, p.repo,
       LOCALTIMESTAMP - make_interval(hours => p.hours), LOCALTIMESTAMP - make_interval(hours => p.hours)
FROM demo_posts p JOIN u ON u.name = p.author
ORDER BY p.hours DESC;

CREATE TEMP TABLE pst ON COMMIT DROP AS
    SELECT p.k, ps.id, ps.created_at, p.likes, p.tech, p.tags
    FROM demo_posts p JOIN u ON u.name = p.author
    JOIN posts ps ON ps.user_id = u.id AND ps.content = p.content;

INSERT INTO post_tech_tags (post_id, tag) SELECT pst.id, unnest(pst.tech) FROM pst;
INSERT INTO post_hashtags (post_id, tag) SELECT pst.id, unnest(pst.tags) FROM pst;

-- 좋아요: 글마다 정한 개수만큼, 작성자를 뺀 회원 중에서 고정된 순서로 고른다
INSERT INTO post_like (post_id, user_id, created_at)
SELECT x.id, x.user_id, x.created_at + make_interval(mins => 20 * x.rn::int)
FROM (
    SELECT pst.id, pst.likes, pst.created_at, u.id AS user_id,
           row_number() OVER (PARTITION BY pst.id ORDER BY md5(pst.k::text || u.name)) AS rn
    FROM pst JOIN posts ps ON ps.id = pst.id
    JOIN u ON u.id <> ps.user_id
) x
WHERE x.rn <= x.likes;

-- 댓글
CREATE TEMP TABLE demo_comments (
    post_k  int NOT NULL,
    author  varchar(50) NOT NULL,
    reply_to varchar(50),
    after_min int NOT NULL,
    content text NOT NULL
) ON COMMIT DROP;

INSERT INTO demo_comments VALUES
(1, 'yunho-choi', NULL, 40, 'OSIV 꺼 두는 거 저희 팀도 신입 온보딩 체크리스트에 있어요. 로그로 안 보여서 더 무섭죠.'),
(1, 'minjae-kang', NULL, 90, '혹시 OSIV를 끄고 나서 LazyInitializationException은 어떻게 정리하셨나요?'),
(1, 'jihoon-han', 'minjae-kang', 120, '서비스에서 DTO로 바꿔서 반환하도록 했어요. 화면에 필요한 건 fetch join이나 전용 조회 쿼리로 미리 읽습니다.'),
(1, 'jiwon-kim', NULL, 200, '제 프로젝트에도 켜져 있네요... 바로 확인해 보겠습니다!'),
(2, 'jihoon-han', NULL, 30, '바뀐 버전도 게시판에 올려 주세요. 한 번 더 볼게요!'),
(2, 'seungwoo-oh', NULL, 60, '저도 첫 문장부터 바꿔 봐야겠어요. 좋은 팁 공유 감사합니다.'),
(3, 'harin-lee', NULL, 25, 'memo부터 붙이지 말라는 말 공감해요. Profiler 먼저!'),
(3, 'seojun.park', NULL, 80, '이미지 width/height 지정은 레이아웃 흔들림까지 잡아 줘서 꼭 챙기게 되더라고요.'),
(4, 'yerin-song', NULL, 35, '서비스 소개는 한두 줄로 짧게, 그다음부터는 소아님이 맡은 화면 이야기만 쓰세요. 면접관은 지원자가 한 일을 궁금해해요.'),
(4, 'gaeun-baek', NULL, 70, '저도 같은 고민했는데 README에 "내가 맡은 부분" 섹션을 따로 만들었어요.'),
(4, 'soah-yoon', 'yerin-song', 100, '서비스 소개는 짧게! 바로 고쳐 볼게요. 감사합니다.'),
(5, 'jihoon-han', NULL, 50, '숫자로 보여 주니까 확 와닿네요. 커서 방식은 정렬 기준이 유일해야 한다는 것도 같이 적어 두면 좋을 것 같아요.'),
(5, 'minjae-kang', 'jihoon-han', 75, '맞아요, created_at만 쓰면 같은 시각 글이 빠져서 id를 같이 넣었습니다!'),
(6, 'jiwon-kim', NULL, 30, '지원 준비 중입니다! 온보딩 1:1이 정말 매력적이네요.'),
(6, 'seungwoo-oh', NULL, 90, '졸업 예정자도 지원 가능한가요?'),
(6, 'finroad', 'seungwoo-oh', 150, '네, 2027년 2월 이전 졸업 예정이시면 지원하실 수 있어요.'),
(7, 'daeun-jeong', NULL, 45, '와 축하해요! 알림톡 비용은 저도 궁금하네요. 웹 푸시로 일부 옮기는 건 어때요?'),
(7, 'taeyang-lim', 'daeun-jeong', 80, '웹 푸시 고민 중이에요. iOS 지원이 아직 걸려서요.'),
(8, 'yerin-song', NULL, 60, '실제 사용자 인터뷰를 하셨다는 게 정말 대단해요. 이력서에도 꼭 넣으세요.'),
(9, 'harin-lee', NULL, 40, '저는 "전공 지식을 개발에 어떻게 썼는지" 사례 하나로 답했어요. 길게 설명하기보다 사례 하나가 강한 것 같아요.'),
(9, 'yerin-song', NULL, 120, '경영학 전공은 데이터와 사용자 이해 쪽으로 연결하기 좋아요. 가계부 대시보드 이야기를 꼭 하세요!'),
(10, 'jihoon-han', NULL, 30, 'JdbcCursorItemReader로 바꾼 효과가 가장 컸다는 거 공감합니다.'),
(10, 'jiwon-kim', NULL, 90, '파티셔닝할 때 가맹점마다 거래량 차이가 크면 어떻게 나누셨나요?'),
(10, 'yunho-choi', 'jiwon-kim', 130, '거래량 기준으로 미리 범위를 계산해서 나눴어요. 처음엔 id로만 나눴다가 한 스레드만 오래 걸리더라고요.'),
(12, 'taeyang-lim', NULL, 20, 'SameSite=None; Secure 설정이랑 CORS의 allowCredentials 둘 다 확인해 보세요. 저도 그거였어요.'),
(12, 'daeun-jeong', 'taeyang-lim', 60, '해결했어요!! SameSite였네요. 감사합니다.'),
(13, 'jiwon-kim', NULL, 15, '제 이력서도 리뷰해 주셔서 감사했습니다!'),
(13, 'soah-yoon', NULL, 50, '팀 프로젝트 부분 저장해 둡니다.'),
(13, 'minjae-kang', NULL, 90, '성과를 문장 맨 앞에. 바로 적용해 볼게요.'),
(14, 'jihoon-han', NULL, 40, '저희는 통합 테스트에서는 @Transactional을 빼고 테스트마다 데이터를 지우는 쪽으로 가요. 지연 로딩처럼 트랜잭션 밖에서 터지는 문제를 테스트가 숨겨 버릴 수 있어서요.'),
(15, 'harin-lee', NULL, 60, '지원하려고 앱 써 보고 있어요. 반찬 사진이 정말 맛있어 보여요.'),
(15, 'gaeun-baek', NULL, 120, '저도 지원했습니다!'),
(17, 'jihoon-han', NULL, 45, '세 가지를 직접 비교한 게 정말 좋아요. 이 내용이 이력서에서 가장 강한 부분입니다.'),
(17, 'yunho-choi', NULL, 100, '락 획득 실패 시 재시도 횟수는 몇 번으로 두셨어요?'),
(17, 'jiwon-kim', 'yunho-choi', 140, '3번으로 두고 그 뒤에는 대기 순번을 보여 줬어요!'),
(19, 'jihoon-han', NULL, 60, '사용자 사전은 파일로 두고 배포할 때 같이 올렸어요. 약어는 동의어 사전으로 묶으면 편합니다.'),
(20, 'jiwon-kim', NULL, 50, '디스코드 웹훅 알림 좋네요. 바로 따라 해 봐야겠어요.'),
(22, 'jihoon-han', NULL, 30, '저도 함께할게요!'),
(22, 'soah-yoon', NULL, 80, '피드백 남겨 주셔서 정말 큰 도움이 됐어요. 감사합니다!'),
(24, 'yerin-song', NULL, 60, '수료 축하해요! 디자인 전공은 프론트엔드에서 정말 큰 무기예요.'),
(24, 'harin-lee', NULL, 90, '축하해요 소아님! 같이 취업해요.'),
(26, 'taeyang-lim', NULL, 40, '"왜?"를 한 번 더 묻는 습관 공감합니다. 어떤 도구 쓰셨어요?'),
(101, 'jihoon-han', NULL, 20, 'isHeldByCurrentThread 확인까지 넣으신 거 좋네요. 락 대기 시간 3초는 부하 테스트로 정하신 건가요?'),
(101, 'jiwon-kim', 'jihoon-han', 45, '네, 1초·3초·5초로 돌려 보고 실패율과 응답 시간이 가장 균형 잡힌 3초로 정했어요!'),
(101, 'seungwoo-oh', NULL, 70, '코드로 보니까 이해가 훨씬 잘 돼요. 저장소 구경하러 갑니다.'),
(102, 'jihoon-han', NULL, 30, '정렬 기준이 id 하나라 깔끔하네요. 최신순 정렬이 created_at이면 id를 보조 키로 꼭 넣어 주세요.'),
(103, 'daeun-jeong', NULL, 40, '헬스체크 실패 시 알림은 어떻게 보내세요?'),
(103, 'taeyang-lim', 'daeun-jeong', 75, '컨테이너 상태를 cron으로 확인해서 unhealthy면 디스코드 웹훅을 쏴요.'),
(104, 'jiwon-kim', NULL, 25, '멱등키 저장과 환불 호출 사이에 서버가 죽으면 어떻게 되나요?'),
(104, 'yunho-choi', 'jiwon-kim', 60, '좋은 질문이에요. 실제로는 요청을 먼저 PENDING으로 저장하고, 결과를 받은 뒤 갱신해요. 공유용으로 줄인 코드라 빠졌네요.'),
(106, 'taeyang-lim', NULL, 15, 'resuit code도 써 보세요. 저장소 연결까지 한 번에 돼서 코드 공유할 때 좋아요.'),
(108, 'jihoon-han', NULL, 35, '플러시와 변경 감지를 건너뛰어서 조회가 많은 화면에선 차이가 나요. 무엇보다 읽기 전용이라는 의도가 코드에 드러나서 저는 붙이는 편입니다.'),
(109, 'yunho-choi', NULL, 50, '저희도 옮겼어요. 테스트 시간은 늘었지만 운영에서만 나는 쿼리 문제를 미리 잡게 됐어요.');

INSERT INTO comments (post_id, user_id, content, created_at, updated_at)
SELECT pst.id, u.id, c.content,
       pst.created_at + make_interval(mins => c.after_min), pst.created_at + make_interval(mins => c.after_min)
FROM demo_comments c JOIN pst ON pst.k = c.post_k JOIN u ON u.name = c.author
WHERE c.reply_to IS NULL;

INSERT INTO comments (post_id, parent_id, user_id, content, created_at, updated_at)
SELECT pst.id, parent.id, u.id, c.content,
       pst.created_at + make_interval(mins => c.after_min), pst.created_at + make_interval(mins => c.after_min)
FROM demo_comments c JOIN pst ON pst.k = c.post_k JOIN u ON u.name = c.author
JOIN u pu ON pu.name = c.reply_to
JOIN comments parent ON parent.post_id = pst.id AND parent.user_id = pu.id AND parent.parent_id IS NULL
WHERE c.reply_to IS NOT NULL;

-- 11. 결과 확인
SELECT '회원' AS 항목, count(*) FROM users WHERE email LIKE '%@demo.resuit.kr'
UNION ALL SELECT '공통 이력서', count(*) FROM common_resumes WHERE user_id IN (SELECT id FROM u)
UNION ALL SELECT '공고', count(*) FROM jb
UNION ALL SELECT '특화 이력서', count(*) FROM target_resumes WHERE user_id IN (SELECT id FROM u)
UNION ALL SELECT '지원', count(*) FROM job_applications WHERE user_id IN (SELECT id FROM u)
UNION ALL SELECT '피드백 요청', count(*) FROM fr
UNION ALL SELECT '피드백 댓글', count(*) FROM feedback_comments WHERE feedback_id IN (SELECT id FROM fr)
UNION ALL SELECT '피드 글', count(*) FROM pst
UNION ALL SELECT '피드 댓글', count(*) FROM comments WHERE post_id IN (SELECT id FROM pst)
UNION ALL SELECT '피드 좋아요', count(*) FROM post_like WHERE post_id IN (SELECT id FROM pst);

COMMIT;
