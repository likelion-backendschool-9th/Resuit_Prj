-- 현직자 경력 연차(mentors.career_years)를 지운다. 경력은 프로필 경력(user_careers)으로 계산한다.
-- 자동 실행되지 않는다. DDL_AUTO=update여도 컬럼을 지우지 않으므로 DB에 직접 적용한다.
-- 컬럼은 NULL을 허용해서 적용하지 않아도 오류는 나지 않는다. 다시 실행해도 오류가 나지 않는다.
BEGIN;

ALTER TABLE mentors DROP COLUMN IF EXISTS career_years;

COMMIT;
