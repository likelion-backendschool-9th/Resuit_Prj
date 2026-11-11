-- 현직자 직무를 직접 입력에서 BACKEND, FRONTEND, FULLSTACK 선택으로 바꾼다.
-- 예전에 직접 입력한 값은 단어를 보고 바꾸고, 어느 쪽인지 알 수 없는 값은 그대로 둔다.
-- 그대로 둔 현직자는 직무별 목록에는 나오지 않고 전체 목록에만 나오며, 현직자 인증 화면에서 직무를 다시 고르면 된다.
-- 이미 바꾼 값은 건드리지 않으므로 다시 실행해도 된다.
UPDATE mentors
SET job_title = CASE
    WHEN job_title ~* '풀스택|full[ -]?stack' THEN 'FULLSTACK'
    WHEN job_title ~* '프론트|프런트|front[ -]?end|\mfe\M' THEN 'FRONTEND'
    WHEN job_title ~* '백엔드|백앤드|back[ -]?end|서버|server|\mbe\M' THEN 'BACKEND'
    ELSE job_title
END
WHERE job_title NOT IN ('BACKEND', 'FRONTEND', 'FULLSTACK');
