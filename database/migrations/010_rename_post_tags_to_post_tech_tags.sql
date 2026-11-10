-- 기존 post_tags 테이블을 기술 태그 전용 이름 post_tech_tags로 바꾼다.
-- 이미 post_tech_tags만 있는 DB에서는 아무 것도 하지 않는다.
DO $$
BEGIN
  IF to_regclass('public.post_tags') IS NOT NULL
     AND to_regclass('public.post_tech_tags') IS NULL THEN
    ALTER TABLE post_tags RENAME TO post_tech_tags;
  END IF;
END $$;

ALTER INDEX IF EXISTS idx_post_tags_tag_post RENAME TO idx_post_tech_tags_tag_post;
