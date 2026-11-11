-- 배지 시연용 데이터: 리뷰어 / 우수 리뷰어 / 미달 계정과 채택·베스트 리뷰
--
-- 실행 (로컬 Docker Compose):
--   Get-Content database/demo/badge-verification.sql -Raw |
--     docker compose exec -T db psql -U "$env:POSTGRES_USER" -d "$env:POSTGRES_DB"
--
-- 실행 (서버 배포 폴더, demo-seed와 동일 방식):
--   docker compose --env-file .env -f compose.yml -f compose.prod.yml exec -T db \
--     sh -c 'psql -v ON_ERROR_STOP=1 -U "$POSTGRES_USER" -d "$POSTGRES_DB"' \
--     < database/demo/badge-verification.sql
--
-- - 배지 시연 회원 이메일은 모두 @badge.demo.resuit.test 이다.
--   demo-seed.sql(@demo.resuit.test)과 섞이지 않으며, 다시 실행하면 이 시연만 지우고 처음 상태로 되돌린다.
-- - 같은 아이디를 실제 회원이 이미 쓰고 있으면 아무것도 바꾸지 않고 멈춘다.
-- - 시연 계정 비밀번호는 demo-seed와 같다. 평문은 이 파일에 남기지 않고 따로 전달한다.
-- - 김민준: 채택 5 / 요청자 3 → 리뷰어
--   이서연: 채택 15 / 요청자 8 / 베스트 1 → 우수 리뷰어
--   박지훈: 채택 4 / 요청자 2 → 미달
-- - 채택 시각은 약 4개월 전이라 직전 두 분기 평가 창에 들어간다.
-- - 홈 피드에 리뷰어 3명의 글을 올려 두었다. 이름·아바타를 누르면 프로필로 간다.

SET client_encoding = 'UTF8';
SET TIME ZONE 'Asia/Seoul';
BEGIN;

CREATE TEMP TABLE badge_demo_users (
    username    varchar(50) PRIMARY KEY,
    nickname    varchar(50) NOT NULL,
    kind        varchar(20) NOT NULL, -- reviewer | requester
    intro       text
) ON COMMIT DROP;

INSERT INTO badge_demo_users VALUES
-- 리뷰어(검증 대상)
('minjun-kim',   '김민준', 'reviewer',  '채택 5건·요청자 3명으로 리뷰어 기준을 채운 시연 계정입니다.'),
('seoyeon-lee',  '이서연', 'reviewer',  '채택 15건·요청자 8명·베스트 1회로 우수 리뷰어 기준을 채운 시연 계정입니다.'),
('jihoon-park',  '박지훈', 'reviewer',  '채택 4건·요청자 2명으로 리뷰어 기준에 미달인 시연 계정입니다.'),
-- 요청자(서로 다른 요청자 수를 만들기 위한 계정)
('yujin-choi',   '최유진', 'requester', '배지 시연용 피드백 요청자입니다.'),
('daeun-jung',   '정다은', 'requester', '배지 시연용 피드백 요청자입니다.'),
('hyunwoo-kang', '강현우', 'requester', '배지 시연용 피드백 요청자입니다.'),
('sua-han',      '한수아', 'requester', '배지 시연용 피드백 요청자입니다.'),
('jimin-yoon',   '윤지민', 'requester', '배지 시연용 피드백 요청자입니다.'),
('junseo-oh',    '오준서', 'requester', '배지 시연용 피드백 요청자입니다.'),
('harin-song',   '송하린', 'requester', '배지 시연용 피드백 요청자입니다.'),
('taeyang-oh',   '오태양', 'requester', '배지 시연용 피드백 요청자입니다.');

-- 1. 실제 회원이 같은 아이디를 쓰고 있으면 멈춘다
DO $$
DECLARE taken text;
BEGIN
    SELECT string_agg(u.username, ', ') INTO taken
    FROM users u
    JOIN badge_demo_users d ON d.username = u.username
    WHERE u.email IS NULL OR u.email NOT LIKE '%@badge.demo.resuit.test';
    IF taken IS NOT NULL THEN
        RAISE EXCEPTION '실제 회원이 이미 쓰는 아이디가 있어 배지 시연 데이터를 넣지 않았어요: %', taken;
    END IF;
END $$;

-- 2. 이전에 넣은 배지 시연 데이터를 지운다
CREATE TEMP TABLE old_badge_users ON COMMIT DROP AS
    SELECT id FROM users WHERE email LIKE '%@badge.demo.resuit.test';
CREATE TEMP TABLE old_badge_requests ON COMMIT DROP AS
    SELECT id FROM feedback_requests WHERE user_id IN (SELECT id FROM old_badge_users);
CREATE TEMP TABLE old_badge_comments ON COMMIT DROP AS
    SELECT comment_id FROM feedback_comments
    WHERE feedback_id IN (SELECT id FROM old_badge_requests)
       OR user_id IN (SELECT id FROM old_badge_users);

CREATE TEMP TABLE old_badge_posts ON COMMIT DROP AS
    SELECT id FROM posts WHERE user_id IN (SELECT id FROM old_badge_users);

DELETE FROM best_reviews WHERE comment_id IN (SELECT comment_id FROM old_badge_comments);
DELETE FROM feedback_comment_likes
WHERE user_id IN (SELECT id FROM old_badge_users)
   OR comment_id IN (SELECT comment_id FROM old_badge_comments);
DELETE FROM feedback_comments WHERE parent_id IS NOT NULL
  AND (feedback_id IN (SELECT id FROM old_badge_requests) OR user_id IN (SELECT id FROM old_badge_users));
DELETE FROM feedback_comments
WHERE feedback_id IN (SELECT id FROM old_badge_requests) OR user_id IN (SELECT id FROM old_badge_users);
DELETE FROM feedback_request_sections WHERE request_id IN (SELECT id FROM old_badge_requests);
DELETE FROM feedback_requests WHERE id IN (SELECT id FROM old_badge_requests);

DELETE FROM comments WHERE parent_id IS NOT NULL
  AND (post_id IN (SELECT id FROM old_badge_posts) OR user_id IN (SELECT id FROM old_badge_users));
DELETE FROM comments
WHERE post_id IN (SELECT id FROM old_badge_posts) OR user_id IN (SELECT id FROM old_badge_users);
DELETE FROM post_like
WHERE post_id IN (SELECT id FROM old_badge_posts) OR user_id IN (SELECT id FROM old_badge_users);
DELETE FROM posts WHERE id IN (SELECT id FROM old_badge_posts);

UPDATE users SET displayed_badge_id = NULL, pinned_common_resume_id = NULL
WHERE id IN (SELECT id FROM old_badge_users);
DELETE FROM user_badge WHERE user_id IN (SELECT id FROM old_badge_users);
DELETE FROM user_badge_history WHERE user_id IN (SELECT id FROM old_badge_users);
DELETE FROM common_resumes WHERE user_id IN (SELECT id FROM old_badge_users);
DELETE FROM users WHERE id IN (SELECT id FROM old_badge_users);

-- 3. 회원 (비밀번호 해시는 demo-seed와 동일)
INSERT INTO users (id, username, email, password, role, nickname, self_introduction, region, subscription, created_at, updated_at)
SELECT gen_random_uuid(), d.username, d.username || '@badge.demo.resuit.test',
       '{bcrypt}$2a$10$3PleSKCU/Eh1LWMpAH.KWuBiQfcnTseogpduGtaJw10tH.3Q5u1TS',
       'USER', d.nickname, d.intro, 'SEOUL', false,
       LOCALTIMESTAMP - INTERVAL '8 months',
       LOCALTIMESTAMP - INTERVAL '8 months'
FROM badge_demo_users d;

CREATE TEMP TABLE bu ON COMMIT DROP AS
    SELECT username AS name, id FROM users WHERE email LIKE '%@badge.demo.resuit.test';

-- 4. 요청자 공통 이력서
INSERT INTO common_resumes (user_id, title, field, content, is_public, created_at, updated_at)
SELECT bu.id, r.title, r.field, '{}'::jsonb, true,
       LOCALTIMESTAMP - INTERVAL '6 months', LOCALTIMESTAMP - INTERVAL '6 months'
FROM (VALUES
    ('yujin-choi',   '프론트엔드 개발자 이력서', 'FRONTEND'),
    ('daeun-jung',   '백엔드 개발자 이력서', 'BACKEND'),
    ('hyunwoo-kang', '풀스택 개발자 이력서', 'FULLSTACK'),
    ('sua-han',      'React 프로젝트 이력서', 'FRONTEND'),
    ('jimin-yoon',   'Java 서버 개발 이력서', 'BACKEND'),
    ('junseo-oh',    '서비스 개발 이력서', 'FULLSTACK'),
    ('harin-song',   '웹 접근성 프로젝트 이력서', 'FRONTEND'),
    ('taeyang-oh',   '데이터 플랫폼 개발 이력서', 'BACKEND')
) AS r(username, title, field)
JOIN bu ON bu.name = r.username;

-- 홈 피드: 리뷰어 3명 글 (이름·아바타 클릭 → 프로필)
INSERT INTO posts (user_id, type, content, created_at, updated_at)
SELECT bu.id, 'GENERAL', p.content,
       LOCALTIMESTAMP - make_interval(hours => p.hours),
       LOCALTIMESTAMP - make_interval(hours => p.hours)
FROM (VALUES
    ('seoyeon-lee', 2,
     E'이력서 피드백을 줄 때 숫자와 맥락을 같이 적으면 채택률이 확 올라가더라고요.\n\n배지 시연용 우수 리뷰어 계정입니다. 이름이나 아바타를 눌러 프로필을 확인해 보세요.'),
    ('minjun-kim', 5,
     E'리뷰어 배지 기준(채택·서로 다른 요청자)을 채운 시연 계정입니다.\n\n홈에서 이름·아바타를 누르면 프로필로 이동합니다.'),
    ('jihoon-park', 8,
     E'채택이 조금 모자라 리뷰어 배지를 받지 못한 시연 계정입니다.\n\n프로필에서 보유 배지가 비어 있는 상태를 확인할 수 있어요.')
) AS p(author, hours, content)
JOIN bu ON bu.name = p.author;

-- 5. 피드백 요청 (채택 1요청 1댓글 구조)
-- req_key: 리뷰어별로 묶어서 나중에 댓글을 붙인다.
CREATE TEMP TABLE badge_reqs (
    req_key   text PRIMARY KEY,
    requester varchar(50) NOT NULL,
    category  varchar(20) NOT NULL,
    section   varchar(30) NOT NULL,
    content   text NOT NULL
) ON COMMIT DROP;

INSERT INTO badge_reqs VALUES
-- minjun-kim 5건 / 요청자 3명 (yujin, daeun, hyunwoo)
('mj-1', 'yujin-choi',   'FRONTEND',  'PROJECTS',     '프로젝트 성과를 더 구체적으로 설명하고 싶어요. 배지 시연용 요청입니다.'),
('mj-2', 'daeun-jung',   'BACKEND',   'SKILLS',       '백엔드 API 개선 경험이 잘 드러나는지 봐 주세요. 배지 시연용 요청입니다.'),
('mj-3', 'hyunwoo-kang', 'FULLSTACK', 'INTRODUCTION', '협업 경험을 이력서에 어떻게 풀어야 할지 고민입니다. 배지 시연용 요청입니다.'),
('mj-4', 'yujin-choi',   'FRONTEND',  'PROJECTS',     '포트폴리오 소개 문장이 너무 길지 않은지 봐주세요. 배지 시연용 요청입니다.'),
('mj-5', 'daeun-jung',   'BACKEND',   'SKILLS',       '기술 스택 나열이 적절한지 의견 부탁드립니다. 배지 시연용 요청입니다.'),
-- seoyeon-lee 15건 / 요청자 8명
('sy-01', 'yujin-choi',   'FRONTEND',  'INTRODUCTION', '장애 대응 경험을 지원 직무와 연결하고 싶습니다. 배지 시연용 요청입니다.'),
('sy-02', 'daeun-jung',   'BACKEND',   'PROJECTS',     '데이터 처리 프로젝트의 핵심 성과가 전달되는지 궁금합니다. 배지 시연용 요청입니다.'),
('sy-03', 'hyunwoo-kang', 'FULLSTACK', 'SKILLS',       '자기소개 문단이 지원 동기를 보여주는지 확인해 주세요. 배지 시연용 요청입니다.'),
('sy-04', 'sua-han',      'FRONTEND',  'INTRODUCTION', '프로젝트 성과 수치를 어떻게 쓰면 좋을까요? 배지 시연용 요청입니다.'),
('sy-05', 'jimin-yoon',   'BACKEND',   'PROJECTS',     'API 개선 경험이 잘 드러나는지 봐 주세요. 배지 시연용 요청입니다.'),
('sy-06', 'junseo-oh',    'FULLSTACK', 'SKILLS',       '협업 경험을 어떻게 풀면 좋을지 조언 부탁드립니다. 배지 시연용 요청입니다.'),
('sy-07', 'harin-song',   'FRONTEND',  'INTRODUCTION', '소개 문장이 너무 길지 않은지 봐주세요. 배지 시연용 요청입니다.'),
('sy-08', 'taeyang-oh',   'BACKEND',   'PROJECTS',     '기술 스택 나열 방식이 적절한지 궁금합니다. 배지 시연용 요청입니다.'),
('sy-09', 'yujin-choi',   'FRONTEND',  'SKILLS',       '장애 대응 경험을 직무와 연결하고 싶습니다. 배지 시연용 요청입니다.'),
('sy-10', 'daeun-jung',   'BACKEND',   'INTRODUCTION', '데이터 처리 성과가 전달되는지 확인해 주세요. 배지 시연용 요청입니다.'),
('sy-11', 'hyunwoo-kang', 'FULLSTACK', 'PROJECTS',     '지원 동기가 충분한지 봐 주세요. 배지 시연용 요청입니다.'),
('sy-12', 'sua-han',      'FRONTEND',  'SKILLS',       '성과 수치 표현을 다듬고 싶습니다. 배지 시연용 요청입니다.'),
('sy-13', 'jimin-yoon',   'BACKEND',   'INTRODUCTION', 'API 경험을 더 잘 드러내고 싶습니다. 배지 시연용 요청입니다.'),
('sy-14', 'junseo-oh',    'FULLSTACK', 'PROJECTS',     '협업 문장을 정리하고 싶습니다. 배지 시연용 요청입니다.'),
('sy-15', 'harin-song',   'FRONTEND',  'SKILLS',       '소개 문장을 짧게 다듬고 싶습니다. 배지 시연용 요청입니다.'),
-- jihoon-park 4건 / 요청자 2명
('jh-1', 'yujin-choi', 'FRONTEND', 'INTRODUCTION', '기술 스택 나열이 적절한지 봐 주세요. 배지 시연용 요청입니다.'),
('jh-2', 'daeun-jung', 'BACKEND',  'PROJECTS',     '장애 대응 경험을 연결하고 싶습니다. 배지 시연용 요청입니다.'),
('jh-3', 'yujin-choi', 'FRONTEND', 'SKILLS',       '데이터 처리 성과가 보이는지 궁금합니다. 배지 시연용 요청입니다.'),
('jh-4', 'daeun-jung', 'BACKEND',  'INTRODUCTION', '지원 동기가 충분한지 확인해 주세요. 배지 시연용 요청입니다.'),
-- 재평가용 미채택 요청 (요청자 yujin-choi)
('pending-mj', 'yujin-choi', 'FRONTEND', 'PROJECTS', '재평가 시연용 미채택 요청입니다. (김민준)'),
('pending-sy', 'yujin-choi', 'FRONTEND', 'PROJECTS', '재평가 시연용 미채택 요청입니다. (이서연)'),
('pending-jh', 'yujin-choi', 'FRONTEND', 'PROJECTS', '재평가 시연용 미채택 요청입니다. (박지훈)');

INSERT INTO feedback_requests (user_id, common_resume_id, content, category, created_at, updated_at)
SELECT bu.id, cr.id, r.content, r.category,
       LOCALTIMESTAMP - INTERVAL '5 months', LOCALTIMESTAMP - INTERVAL '5 months'
FROM badge_reqs r
JOIN bu ON bu.name = r.requester
JOIN common_resumes cr ON cr.user_id = bu.id;

CREATE TEMP TABLE brq ON COMMIT DROP AS
    SELECT fr.id AS request_id, r.req_key, r.requester, r.section, r.category
    FROM feedback_requests fr
    JOIN bu ON bu.id = fr.user_id
    JOIN badge_reqs r ON r.requester = bu.name AND r.content = fr.content;

INSERT INTO feedback_request_sections (request_id, section_type)
SELECT request_id, section FROM brq;

-- 6. 채택된 피드백 댓글
INSERT INTO feedback_comments (feedback_id, user_id, section, content, is_adopted, adopted_at, created_at, updated_at)
SELECT brq.request_id, rev.id, brq.section, c.content, true,
       LOCALTIMESTAMP - INTERVAL '4 months',
       LOCALTIMESTAMP - INTERVAL '5 months',
       LOCALTIMESTAMP - INTERVAL '5 months'
FROM (VALUES
    ('mj-1', 'minjun-kim',  '증가율과 함께 비교 기간과 측정 지표를 적으면 성과가 더 명확해집니다.'),
    ('mj-2', 'minjun-kim',  '응답 시간이 얼마나 줄었는지와 측정 방법을 한 문장으로 추가해 보세요.'),
    ('mj-3', 'minjun-kim',  '맡은 역할과 팀원과의 의사결정 과정을 나누어 쓰면 기여도가 선명해집니다.'),
    ('mj-4', 'minjun-kim',  '첫 문장에 해결한 문제를 두고 구현 기술은 다음 문장으로 나누면 읽기 편합니다.'),
    ('mj-5', 'minjun-kim',  '단순 목록보다 각 기술을 어디에 적용했는지 프로젝트 경험과 연결해 주세요.'),
    ('sy-01', 'seoyeon-lee', '장애 원인, 조치, 재발 방지 순서로 정리하면 문제 해결 과정이 잘 보입니다.'),
    ('sy-02', 'seoyeon-lee', '처리량과 정확도 같은 수치를 기준 환경과 함께 적어 보세요.'),
    ('sy-03', 'seoyeon-lee', '입사 후 기여할 수 있는 경험을 먼저 제시하면 지원 동기가 더 설득력 있게 읽힙니다.'),
    ('sy-04', 'seoyeon-lee', '증가율과 함께 비교 기간과 측정 지표를 적으면 성과가 더 명확해집니다.'),
    ('sy-05', 'seoyeon-lee', '응답 시간이 얼마나 줄었는지와 측정 방법을 한 문장으로 추가해 보세요.'),
    ('sy-06', 'seoyeon-lee', '맡은 역할과 팀원과의 의사결정 과정을 나누어 쓰면 기여도가 선명해집니다.'),
    ('sy-07', 'seoyeon-lee', '첫 문장에 해결한 문제를 두고 구현 기술은 다음 문장으로 나누면 읽기 편합니다.'),
    ('sy-08', 'seoyeon-lee', '단순 목록보다 각 기술을 어디에 적용했는지 프로젝트 경험과 연결해 주세요.'),
    ('sy-09', 'seoyeon-lee', '장애 원인, 조치, 재발 방지 순서로 정리하면 문제 해결 과정이 잘 보입니다.'),
    ('sy-10', 'seoyeon-lee', '처리량과 정확도 같은 수치를 기준 환경과 함께 적어 보세요.'),
    ('sy-11', 'seoyeon-lee', '입사 후 기여할 수 있는 경험을 먼저 제시하면 지원 동기가 더 설득력 있게 읽힙니다.'),
    ('sy-12', 'seoyeon-lee', '증가율과 함께 비교 기간과 측정 지표를 적으면 성과가 더 명확해집니다.'),
    ('sy-13', 'seoyeon-lee', '응답 시간이 얼마나 줄었는지와 측정 방법을 한 문장으로 추가해 보세요.'),
    ('sy-14', 'seoyeon-lee', '맡은 역할과 팀원과의 의사결정 과정을 나누어 쓰면 기여도가 선명해집니다.'),
    ('sy-15', 'seoyeon-lee', '첫 문장에 해결한 문제를 두고 구현 기술은 다음 문장으로 나누면 읽기 편합니다.'),
    ('jh-1', 'jihoon-park', '단순 목록보다 각 기술을 어디에 적용했는지 프로젝트 경험과 연결해 주세요.'),
    ('jh-2', 'jihoon-park', '장애 원인, 조치, 재발 방지 순서로 정리하면 문제 해결 과정이 잘 보입니다.'),
    ('jh-3', 'jihoon-park', '처리량과 정확도 같은 수치를 기준 환경과 함께 적어 보세요.'),
    ('jh-4', 'jihoon-park', '입사 후 기여할 수 있는 경험을 먼저 제시하면 지원 동기가 더 설득력 있게 읽힙니다.')
) AS c(req_key, reviewer, content)
JOIN brq ON brq.req_key = c.req_key
JOIN bu rev ON rev.name = c.reviewer;

-- 7. 재평가용 미채택 댓글
INSERT INTO feedback_comments (feedback_id, user_id, section, content, is_adopted, created_at, updated_at)
SELECT brq.request_id, rev.id, brq.section, c.content, false,
       LOCALTIMESTAMP - INTERVAL '1 day', LOCALTIMESTAMP - INTERVAL '1 day'
FROM (VALUES
    ('pending-mj', 'minjun-kim',  '재평가 시연용 미채택 피드백입니다.'),
    ('pending-sy', 'seoyeon-lee', '재평가 시연용 미채택 피드백입니다.'),
    ('pending-jh', 'jihoon-park', '재평가 시연용 미채택 피드백입니다.')
) AS c(req_key, reviewer, content)
JOIN brq ON brq.req_key = c.req_key
JOIN bu rev ON rev.name = c.reviewer;

-- 8. 이서연 베스트 리뷰
-- 평가 창은 이번 분기 시작일 기준 직전 6개월이다. 그 안의 빈 monthly 1위를 고른다.
INSERT INTO best_reviews (comment_id, period, period_start, rank_no, created_at)
SELECT fc.comment_id, 'monthly', slot.period_start, 1, LOCALTIMESTAMP - INTERVAL '3 months'
FROM feedback_comments fc
JOIN bu ON bu.id = fc.user_id AND bu.name = 'seoyeon-lee'
JOIN brq ON brq.request_id = fc.feedback_id AND brq.req_key = 'sy-01'
CROSS JOIN LATERAL (
    SELECT gs::date AS period_start
    FROM generate_series(
        (date_trunc('quarter', LOCALTIMESTAMP) - INTERVAL '6 months')::date,
        (date_trunc('quarter', LOCALTIMESTAMP) - INTERVAL '1 month')::date,
        INTERVAL '1 month'
    ) AS gs
    WHERE NOT EXISTS (
        SELECT 1 FROM best_reviews br
        WHERE LOWER(br.period) = 'monthly'
          AND br.period_start = gs::date
          AND br.rank_no = 1
    )
    ORDER BY gs DESC
    LIMIT 1
) AS slot
WHERE fc.is_adopted
LIMIT 1;

-- 9. 보유 배지 지급 + 대표 배지 (시연에서 바로 보이게)
INSERT INTO user_badge (user_id, badge_id, acquired_at, is_eligible, status, continuous_since)
SELECT bu.id, b.id, LOCALTIMESTAMP - INTERVAL '4 months', true, 'ACTIVE', LOCALTIMESTAMP - INTERVAL '4 months'
FROM bu
JOIN badge b ON b.code = 'REVIEWER'
WHERE bu.name = 'minjun-kim';

INSERT INTO user_badge (user_id, badge_id, acquired_at, is_eligible, status, continuous_since)
SELECT bu.id, b.id, LOCALTIMESTAMP - INTERVAL '4 months', true, 'ACTIVE', LOCALTIMESTAMP - INTERVAL '4 months'
FROM bu
CROSS JOIN badge b
WHERE bu.name = 'seoyeon-lee' AND b.code IN ('REVIEWER', 'EXCELLENT_REVIEWER');

UPDATE users u
SET displayed_badge_id = b.id
FROM badge b
WHERE u.username = 'minjun-kim'
  AND u.email LIKE '%@badge.demo.resuit.test'
  AND b.code = 'REVIEWER';

UPDATE users u
SET displayed_badge_id = b.id
FROM badge b
WHERE u.username = 'seoyeon-lee'
  AND u.email LIKE '%@badge.demo.resuit.test'
  AND b.code = 'EXCELLENT_REVIEWER';

COMMIT;

-- 확인 (주소 입력 없이):
--   홈 → 이서연 / 김민준 / 박지훈 글의 이름·아바타 클릭 → 프로필 배지
--   이서연: 우수 리뷰어(+리뷰어) / 김민준: 리뷰어 / 박지훈: 보유 배지 없음
-- 진행률 API: GET /api/v1/badges/me/reviewer-progress
-- 재평가(선택): 미채택 댓글을 요청자(yujin-choi)로 채택
