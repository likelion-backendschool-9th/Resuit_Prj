-- 인재 찾기 상세 필터(9.2)와 기업 요금제(9.6)에 필요한 컬럼·테이블.
-- 자동 실행되지 않는다. DDL_AUTO가 none인 DB(배포 서버 등)에 직접 적용한다. 다시 실행해도 오류가 나지 않는다.
BEGIN;

-- 회원 지역. 값은 Region enum 이름(SEOUL, BUSAN, DAEGU, INCHEON, GWANGJU, DAEJEON, ULSAN, GYEONGGI, JEJU, ETC)이다.
-- 지역을 추가해도 DB를 바꾸지 않도록 CHECK 제약은 두지 않는다.
ALTER TABLE users ADD COLUMN IF NOT EXISTS region VARCHAR(20);

-- 회원별 GitHub 최근 1년 기여 수. 스케줄러가 매일 갱신하고, 인재 찾기는 이 값만 읽는다.
CREATE TABLE IF NOT EXISTS github_contribution_stats (
    user_id UUID PRIMARY KEY REFERENCES users (id) ON DELETE CASCADE,
    total_contributions INT NOT NULL,
    synced_at TIMESTAMP NOT NULL
);

-- 기업 요금제. 엔티티는 이미 있으므로, 테이블이 없는 DB에서만 만든다.
CREATE TABLE IF NOT EXISTS company_subscriptions (
    company_id UUID PRIMARY KEY REFERENCES users (id),
    plan VARCHAR(20) NOT NULL,
    trial_ends_at TIMESTAMP NULL,
    expires_at TIMESTAMP NULL,
    updated_at TIMESTAMP NOT NULL
);

COMMIT;
