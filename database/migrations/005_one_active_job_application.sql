-- PostgreSQL 수동 적용. 기존 활성 중복이 있으면 실패하며 기록을 임의로 삭제하지 않는다.
-- 적용 전 확인:
-- SELECT job_id, user_id, count(*) FROM job_applications
-- WHERE canceled_at IS NULL GROUP BY job_id, user_id HAVING count(*) > 1;
BEGIN;
CREATE UNIQUE INDEX IF NOT EXISTS uq_job_applications_one_active
    ON job_applications (job_id, user_id) WHERE canceled_at IS NULL;
COMMIT;
