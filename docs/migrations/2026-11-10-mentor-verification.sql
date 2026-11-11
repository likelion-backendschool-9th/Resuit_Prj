-- 회사 이메일 인증을 현직자 인증으로 바꾸면서 도메인에 회사명을 둔다. 현직자의 회사명으로 쓴다.
-- 회사명이 비어 있으면 도메인을 그대로 보여 준다.
-- 자동 실행되지 않는다. DDL_AUTO가 none인 DB(배포 서버 등)에 직접 적용한다. 다시 실행해도 오류가 나지 않는다.
BEGIN;

ALTER TABLE company_email_domains ADD COLUMN IF NOT EXISTS company_name VARCHAR(100);

UPDATE company_email_domains SET company_name = '뉴렉처'
WHERE domain = 'newlecture.com' AND company_name IS NULL;

COMMIT;
