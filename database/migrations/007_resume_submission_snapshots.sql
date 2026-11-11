-- 제출 당시 이력서/사진 보존. 기존 지원의 과거 내용은 보정하지 않는다.
BEGIN;
ALTER TABLE common_resumes ADD COLUMN IF NOT EXISTS photo_url VARCHAR(500);
ALTER TABLE target_resumes ADD COLUMN IF NOT EXISTS photo_url VARCHAR(500);
ALTER TABLE job_applications
    ADD COLUMN IF NOT EXISTS submitted_resume_type VARCHAR(20),
    ADD COLUMN IF NOT EXISTS submitted_company_name VARCHAR(100),
    ADD COLUMN IF NOT EXISTS submitted_resume_title VARCHAR(100),
    ADD COLUMN IF NOT EXISTS submitted_resume_snapshot JSONB,
    ADD COLUMN IF NOT EXISTS snapshot_photo_key VARCHAR(500),
    ADD COLUMN IF NOT EXISTS snapshot_expires_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS snapshot_deleted_at TIMESTAMP;

-- 원본 삭제 후에는 두 원본 FK 모두 NULL일 수 있다. Hibernate가 생성한 FK 이름에도 대응한다.
DO $$
DECLARE item RECORD;
BEGIN
    FOR item IN
        SELECT c.conname, c.conrelid::regclass AS table_name
        FROM pg_constraint c
        WHERE c.contype = 'f'
          AND ((c.conrelid = 'job_applications'::regclass
                AND c.confrelid IN ('common_resumes'::regclass, 'target_resumes'::regclass))
               OR (c.conrelid = 'target_resumes'::regclass AND c.confrelid = 'common_resumes'::regclass))
    LOOP
        EXECUTE format('ALTER TABLE %s DROP CONSTRAINT %I', item.table_name, item.conname);
    END LOOP;
    FOR item IN
        SELECT conname FROM pg_constraint
        WHERE conrelid = 'job_applications'::regclass AND contype = 'c'
          AND pg_get_constraintdef(oid) LIKE '%common_resume_id%'
          AND pg_get_constraintdef(oid) LIKE '%target_resume_id%'
    LOOP
        EXECUTE format('ALTER TABLE job_applications DROP CONSTRAINT %I', item.conname);
    END LOOP;
END $$;
ALTER TABLE job_applications
    ADD CONSTRAINT fk_application_common_resume FOREIGN KEY (common_resume_id) REFERENCES common_resumes(id) ON DELETE SET NULL,
    ADD CONSTRAINT fk_application_target_resume FOREIGN KEY (target_resume_id) REFERENCES target_resumes(id) ON DELETE SET NULL,
    ADD CONSTRAINT ck_application_at_most_one_source CHECK (common_resume_id IS NULL OR target_resume_id IS NULL);
ALTER TABLE target_resumes
    ADD CONSTRAINT fk_target_common_resume FOREIGN KEY (common_resume_id) REFERENCES common_resumes(id) ON DELETE SET NULL;
CREATE INDEX IF NOT EXISTS idx_job_application_snapshot_expiry ON job_applications(snapshot_expires_at);
COMMIT;
