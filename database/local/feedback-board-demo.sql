-- 피드백 게시판 목록→상세·프로필 '남긴 피드백' 확인용. 개인 개발 DB 전용.
-- 선행: database/local/review-demo.sql
-- 예약 ID: 요청 900101~900102, 댓글 900101~900103, 이력서 900101
BEGIN;

-- 기존 리뷰 데모 요청에 분야를 채워 탭 필터를 확인할 수 있게 한다.
UPDATE feedback_requests SET category = 'FRONTEND', updated_at = CURRENT_TIMESTAMP
WHERE id IN (900001, 900003, 900004, 900006, 900007, 900010) AND deleted_at IS NULL;
UPDATE feedback_requests SET category = 'BACKEND', updated_at = CURRENT_TIMESTAMP
WHERE id IN (900002, 900005, 900008, 900009, 900011, 900012) AND deleted_at IS NULL;

INSERT INTO feedback_request_sections (request_id, section_type)
SELECT r.id, s.section_type
FROM feedback_requests r
CROSS JOIN (VALUES ('PROJECTS'), ('SKILLS'), ('INTRODUCTION')) AS s(section_type)
WHERE r.id BETWEEN 900001 AND 900012
  AND NOT EXISTS (
    SELECT 1 FROM feedback_request_sections frs
    WHERE frs.request_id = r.id AND frs.section_type = s.section_type
  );

-- 로그인 사용자(davidlee534) 이력서·요청·남긴 피드백
INSERT INTO common_resumes (id, user_id, title, content, is_public, field, created_at)
SELECT 900101, u.id, '게시판 확인용 이력서',
       '{"basic":{"name":"이상호"},"skills":["Java","Spring","React"],"projects":[{"name":"Resuit","summary":"이력서 피드백 서비스"}],"introduction":"백엔드와 프론트를 함께 다루는 개발자입니다."}'::jsonb,
       true, 'BACKEND', CURRENT_TIMESTAMP - INTERVAL '2 days'
FROM users u
WHERE u.username = 'davidlee534'
ON CONFLICT (id) DO UPDATE SET title = EXCLUDED.title, content = EXCLUDED.content, is_public = true;

INSERT INTO feedback_requests (id, user_id, common_resume_id, category, content, created_at)
SELECT 900101, u.id, 900101, 'BACKEND',
       '프로젝트 경험과 기술 스택이 지원 직무와 잘 맞는지 봐 주세요. 목록에서 눌러 상세로 이동되는지 확인하는 데모 요청입니다.',
       CURRENT_TIMESTAMP - INTERVAL '1 day'
FROM users u
WHERE u.username = 'davidlee534'
ON CONFLICT (id) DO UPDATE SET content = EXCLUDED.content, category = EXCLUDED.category, deleted_at = NULL;

INSERT INTO feedback_request_sections (request_id, section_type)
SELECT 900101, s.section_type
FROM (VALUES ('PROJECTS'), ('SKILLS'), ('INTRODUCTION')) AS s(section_type)
WHERE NOT EXISTS (
  SELECT 1 FROM feedback_request_sections frs
  WHERE frs.request_id = 900101 AND frs.section_type = s.section_type
);

-- 다른 사람이 davidlee534 요청에 남긴 댓글(상세 댓글 목록 확인)
INSERT INTO feedback_comments (comment_id, feedback_id, user_id, section, content, is_adopted, adopted_at, created_at)
SELECT 900101, 900101, '00000000-0000-4000-8000-000000900002', 'PROJECTS',
       'Resuit 프로젝트 요약에 본인이 맡은 API와 화면 범위를 한 줄씩 나눠 적으면 역할이 더 분명해질 거예요.',
       false, NULL, CURRENT_TIMESTAMP - INTERVAL '20 hours'
WHERE EXISTS (SELECT 1 FROM feedback_requests WHERE id = 900101)
ON CONFLICT (comment_id) DO UPDATE SET content = EXCLUDED.content, section = EXCLUDED.section;

-- davidlee534가 남긴 피드백 → 프로필 '남긴 피드백'에서 상세로 이동
INSERT INTO feedback_comments (comment_id, feedback_id, user_id, section, content, is_adopted, adopted_at, created_at)
SELECT 900102, 900002, u.id, 'SKILLS',
       '트랜잭션 경계를 요청 단위로 나눈 설명이 좋아요. 충돌 시 사용자에게 보여 준 메시지 예시를 한 줄 넣으면 더 설득력 있습니다.',
       false, NULL, CURRENT_TIMESTAMP - INTERVAL '12 hours'
FROM users u
WHERE u.username = 'davidlee534'
ON CONFLICT (comment_id) DO UPDATE SET content = EXCLUDED.content, section = EXCLUDED.section, user_id = EXCLUDED.user_id;

-- 900003은 이미 채택 댓글이 있을 수 있어 채택하지 않은 댓글로 둔다.
INSERT INTO feedback_comments (comment_id, feedback_id, user_id, section, content, is_adopted, adopted_at, created_at)
SELECT 900103, 900003, u.id, 'INTRODUCTION',
       '소개 첫 문장을 구체 성과로 바꾸면 좋겠어요. 검색 기능을 고친 계기를 앞에 두고, 태도 표현은 뒤로 빼 보세요.',
       false, NULL, CURRENT_TIMESTAMP - INTERVAL '8 hours'
FROM users u
WHERE u.username = 'davidlee534'
ON CONFLICT (comment_id) DO UPDATE SET content = EXCLUDED.content, section = EXCLUDED.section,
  is_adopted = EXCLUDED.is_adopted, adopted_at = EXCLUDED.adopted_at, user_id = EXCLUDED.user_id;

-- 고정 ID 삽입 후 시퀀스 동기화
SELECT setval(pg_get_serial_sequence('common_resumes', 'id'), GREATEST((SELECT MAX(id) FROM common_resumes), 1));
SELECT setval(pg_get_serial_sequence('feedback_requests', 'id'), GREATEST((SELECT MAX(id) FROM feedback_requests), 1));
SELECT setval(pg_get_serial_sequence('feedback_comments', 'comment_id'), GREATEST((SELECT MAX(comment_id) FROM feedback_comments), 1));

COMMIT;
