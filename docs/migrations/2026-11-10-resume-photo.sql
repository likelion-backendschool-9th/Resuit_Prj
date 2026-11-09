-- 이력서 사진. 공통·특화 이력서에 사진 주소(/media/image/{회원ID}/파일)를 둔다. 파일은 백엔드 미디어 폴더에 저장한다.
-- 자동 실행되지 않는다. DDL_AUTO가 none인 DB(배포 서버 등)에 직접 적용한다. 다시 실행해도 오류가 나지 않는다.
BEGIN;

ALTER TABLE common_resumes ADD COLUMN IF NOT EXISTS photo_url VARCHAR(500);
ALTER TABLE target_resumes ADD COLUMN IF NOT EXISTS photo_url VARCHAR(500);

COMMIT;
