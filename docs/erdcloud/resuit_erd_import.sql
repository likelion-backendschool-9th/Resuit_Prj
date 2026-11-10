CREATE TABLE "blog_posts" (
	"id"	BIGINT		NOT NULL,
	"user_id"	UUID		NOT NULL,
	"integration_id"	BIGINT		NOT NULL,
	"guid"	VARCHAR(500)		NOT NULL,
	"title"	VARCHAR(300)		NOT NULL,
	"description"	TEXT		NULL,
	"url"	VARCHAR(500)		NOT NULL,
	"published_at"	TIMESTAMP		NULL,
	"collected_at"	TIMESTAMP		NOT NULL
);

COMMENT ON COLUMN "blog_posts"."id" IS '아이디';

COMMENT ON COLUMN "blog_posts"."user_id" IS '유저 아이디';

COMMENT ON COLUMN "blog_posts"."integration_id" IS '연동 아이디';

COMMENT ON COLUMN "blog_posts"."guid" IS '글 고유값';

COMMENT ON COLUMN "blog_posts"."title" IS '제목';

COMMENT ON COLUMN "blog_posts"."description" IS '요약';

COMMENT ON COLUMN "blog_posts"."url" IS '글 주소';

COMMENT ON COLUMN "blog_posts"."published_at" IS '발행일';

COMMENT ON COLUMN "blog_posts"."collected_at" IS '수집일';

CREATE TABLE "job_applications" (
	"id"	BIGINT		NOT NULL,
	"job_id"	UUID		NOT NULL,
	"user_id"	UUID		NOT NULL,
	"common_resume_id"	BIGINT		NULL,
	"target_resume_id"	BIGINT		NULL,
	"status"	VARCHAR(20)		NOT NULL,
	"applied_at"	TIMESTAMP		NOT NULL,
	"canceled_at"	TIMESTAMP		NULL
);

COMMENT ON COLUMN "job_applications"."id" IS '아이디';

COMMENT ON COLUMN "job_applications"."job_id" IS '공고 아이디';

COMMENT ON COLUMN "job_applications"."user_id" IS '지원자 아이디';

COMMENT ON COLUMN "job_applications"."common_resume_id" IS '공통 이력서 아이디';

COMMENT ON COLUMN "job_applications"."target_resume_id" IS '특화 이력서 아이디';

COMMENT ON COLUMN "job_applications"."status" IS '지원 상태 (applied, canceled)';

COMMENT ON COLUMN "job_applications"."applied_at" IS '지원일';

COMMENT ON COLUMN "job_applications"."canceled_at" IS '취소일';

CREATE TABLE "badge" (
	"id"	INT		NOT NULL,
	"type"	VARCHAR(30)		NULL,
	"code"	VARCHAR(50)		NOT NULL,
	"name"	VARCHAR(50)		NULL,
	"description"	VARCHAR(255)		NULL,
	"icon_url"	VARCHAR(500)		NULL,
	"validity_days"	INT		NULL
);

COMMENT ON COLUMN "badge"."id" IS '아이디';

COMMENT ON COLUMN "badge"."type" IS '배지 종류';

COMMENT ON COLUMN "badge"."code" IS '배지 코드';

COMMENT ON COLUMN "badge"."name" IS '배지 이름';

COMMENT ON COLUMN "badge"."description" IS '설명';

COMMENT ON COLUMN "badge"."icon_url" IS '아이콘 주소';

COMMENT ON COLUMN "badge"."validity_days" IS '유효 기간(일)';

CREATE TABLE "comments" (
	"id"	BIGINT		NOT NULL,
	"post_id"	BIGINT		NOT NULL,
	"user_id"	UUID		NOT NULL,
	"parent_id"	BIGINT		NULL,
	"content"	TEXT		NOT NULL,
	"created_at"	TIMESTAMP		NOT NULL,
	"updated_at"	TIMESTAMP		NULL,
	"deleted_at"	TIMESTAMP		NULL
);

COMMENT ON COLUMN "comments"."id" IS '아이디';

COMMENT ON COLUMN "comments"."post_id" IS '게시글 아이디';

COMMENT ON COLUMN "comments"."user_id" IS '작성자 아이디';

COMMENT ON COLUMN "comments"."parent_id" IS '부모 댓글 아이디';

COMMENT ON COLUMN "comments"."content" IS '내용';

COMMENT ON COLUMN "comments"."created_at" IS '작성일';

COMMENT ON COLUMN "comments"."updated_at" IS '수정일';

COMMENT ON COLUMN "comments"."deleted_at" IS '삭제일';

CREATE TABLE "user_badge" (
	"user_id"	UUID		NOT NULL,
	"badge_id"	INT		NOT NULL,
	"acquired_at"	TIMESTAMP		NOT NULL,
	"is_eligible"	BOOLEAN		NOT NULL,
	"continuous_since"	TIMESTAMP		NULL,
	"expires_at"	TIMESTAMP		NULL
);

COMMENT ON COLUMN "user_badge"."user_id" IS '유저 아이디';

COMMENT ON COLUMN "user_badge"."badge_id" IS '배지 아이디';

COMMENT ON COLUMN "user_badge"."acquired_at" IS '획득일';

COMMENT ON COLUMN "user_badge"."is_eligible" IS '자격 유지 여부';

COMMENT ON COLUMN "user_badge"."continuous_since" IS '연속 유지 시작일';

COMMENT ON COLUMN "user_badge"."expires_at" IS '만료일';

CREATE TABLE "follows" (
	"follower_id"	UUID		NOT NULL,
	"following_id"	UUID		NOT NULL,
	"created_at"	TIMESTAMP		NOT NULL
);

COMMENT ON COLUMN "follows"."follower_id" IS '팔로워 아이디';

COMMENT ON COLUMN "follows"."following_id" IS '팔로잉 아이디';

COMMENT ON COLUMN "follows"."created_at" IS '팔로우 시각';

CREATE TABLE "verifications" (
	"id"	BIGINT		NOT NULL,
	"user_id"	UUID		NOT NULL,
	"type"	VARCHAR(20)		NOT NULL,
	"email"	VARCHAR(255)		NOT NULL,
	"code_hash"	VARCHAR(255)		NOT NULL,
	"expires_at"	TIMESTAMP		NOT NULL,
	"verified_at"	TIMESTAMP		NULL,
	"created_at"	TIMESTAMP		NOT NULL
);

COMMENT ON COLUMN "verifications"."id" IS '아이디';

COMMENT ON COLUMN "verifications"."user_id" IS '유저 아이디';

COMMENT ON COLUMN "verifications"."type" IS '인증 종류 (company_email, employee)';

COMMENT ON COLUMN "verifications"."email" IS '인증 이메일';

COMMENT ON COLUMN "verifications"."code_hash" IS '인증 코드 해시';

COMMENT ON COLUMN "verifications"."expires_at" IS '코드 만료 시각';

COMMENT ON COLUMN "verifications"."verified_at" IS '인증 완료 시각';

COMMENT ON COLUMN "verifications"."created_at" IS '요청일';

CREATE TABLE "credit_payments" (
	"id"	BIGINT		NOT NULL,
	"user_id"	UUID		NOT NULL,
	"amount_krw"	INT		NOT NULL,
	"credits"	INT		NOT NULL,
	"pg_transaction_id"	VARCHAR(100)		NULL,
	"status"	VARCHAR(20)		NOT NULL,
	"created_at"	TIMESTAMP		NOT NULL
);

COMMENT ON COLUMN "credit_payments"."id" IS '아이디';

COMMENT ON COLUMN "credit_payments"."user_id" IS '유저 아이디';

COMMENT ON COLUMN "credit_payments"."amount_krw" IS '결제 금액(원)';

COMMENT ON COLUMN "credit_payments"."credits" IS '충전 크레딧';

COMMENT ON COLUMN "credit_payments"."pg_transaction_id" IS '결제사 거래 아이디';

COMMENT ON COLUMN "credit_payments"."status" IS '결제 상태 (pending, paid, failed, refunded)';

COMMENT ON COLUMN "credit_payments"."created_at" IS '결제일';

CREATE TABLE "integrations" (
	"id"	BIGINT		NOT NULL,
	"user_id"	UUID		NOT NULL,
	"provider"	VARCHAR(20)		NOT NULL,
	"platform"	VARCHAR(30)		NULL,
	"external_id"	VARCHAR(100)		NULL,
	"rss_url"	VARCHAR(500)		NULL,
	"access_token"	VARCHAR(500)		NULL,
	"connected"	BOOLEAN	DEFAULT false	NOT NULL,
	"last_synced_at"	TIMESTAMP		NULL,
	"created_at"	TIMESTAMP		NOT NULL
);

COMMENT ON COLUMN "integrations"."id" IS '아이디';

COMMENT ON COLUMN "integrations"."user_id" IS '유저 아이디';

COMMENT ON COLUMN "integrations"."provider" IS '연동 종류 (github, blog)';

COMMENT ON COLUMN "integrations"."platform" IS '블로그 플랫폼 (velog, tistory, etc)';

COMMENT ON COLUMN "integrations"."external_id" IS '외부 계정 아이디';

COMMENT ON COLUMN "integrations"."rss_url" IS 'RSS 주소';

COMMENT ON COLUMN "integrations"."access_token" IS '액세스 토큰';

COMMENT ON COLUMN "integrations"."connected" IS '연결 여부';

COMMENT ON COLUMN "integrations"."last_synced_at" IS '마지막 동기화 시각';

COMMENT ON COLUMN "integrations"."created_at" IS '생성일';

CREATE TABLE "credit_transactions" (
	"id"	BIGINT		NOT NULL,
	"user_id"	UUID		NOT NULL,
	"type"	VARCHAR(20)		NOT NULL,
	"amount"	INT		NOT NULL,
	"balance_after"	INT		NOT NULL,
	"paid_feedback_request_id"	BIGINT		NULL,
	"credit_payment_id"	BIGINT		NULL,
	"created_at"	TIMESTAMP		NOT NULL
);

COMMENT ON COLUMN "credit_transactions"."id" IS '아이디';

COMMENT ON COLUMN "credit_transactions"."user_id" IS '유저 아이디';

COMMENT ON COLUMN "credit_transactions"."type" IS '내역 종류 (charge, use, earn, refund)';

COMMENT ON COLUMN "credit_transactions"."amount" IS '변동량';

COMMENT ON COLUMN "credit_transactions"."balance_after" IS '변동 후 잔액';

COMMENT ON COLUMN "credit_transactions"."paid_feedback_request_id" IS '유료 피드백 요청 아이디';

COMMENT ON COLUMN "credit_transactions"."credit_payment_id" IS '결제 아이디';

COMMENT ON COLUMN "credit_transactions"."created_at" IS '발생일';

CREATE TABLE "post_like" (
	"post_id"	BIGINT		NOT NULL,
	"user_id"	UUID		NOT NULL,
	"created_at"	TIMESTAMP		NOT NULL
);

COMMENT ON COLUMN "post_like"."post_id" IS '게시글 아이디';

COMMENT ON COLUMN "post_like"."user_id" IS '유저 아이디';

COMMENT ON COLUMN "post_like"."created_at" IS '좋아요 시각';

CREATE TABLE "common_resumes" (
	"id"	BIGINT		NOT NULL,
	"user_id"	UUID		NOT NULL,
	"title"	VARCHAR(100)		NOT NULL,
	"field"	VARCHAR(20)		NULL,
	"content"	JSONB		NOT NULL,
	"is_public"	BOOLEAN	DEFAULT false	NOT NULL,
	"created_at"	TIMESTAMP		NOT NULL,
	"updated_at"	TIMESTAMP		NULL
);

COMMENT ON COLUMN "common_resumes"."id" IS '아이디';

COMMENT ON COLUMN "common_resumes"."user_id" IS '유저 아이디';

COMMENT ON COLUMN "common_resumes"."title" IS '제목';

COMMENT ON COLUMN "common_resumes"."field" IS '직무 분야 (frontend, backend)';

COMMENT ON COLUMN "common_resumes"."content" IS '이력서 내용';

COMMENT ON COLUMN "common_resumes"."is_public" IS '공개 여부';

COMMENT ON COLUMN "common_resumes"."created_at" IS '작성일';

COMMENT ON COLUMN "common_resumes"."updated_at" IS '수정일';

CREATE TABLE "job" (
	"id"	BIGINT		NOT NULL,
	"company_id"	UUID		NOT NULL,
	"type"	VARCHAR(20)		NOT NULL,
	"title"	VARCHAR(100)		NOT NULL,
	"location"	VARCHAR(50)		NULL,
	"employment_type"	VARCHAR(30)		NULL,
	"career"	VARCHAR(30)		NULL,
	"description"	TEXT		NULL,
	"requirements"	TEXT		NULL,
	"preferred"	TEXT		NULL,
	"deadline"	DATE		NULL,
	"status"	VARCHAR(20)		NOT NULL,
	"created_at"	TIMESTAMP		NOT NULL,
	"updated_at"	TIMESTAMP		NULL,
	"deleted_at"	TIMESTAMP		NULL
);

COMMENT ON COLUMN "job"."id" IS '아이디';

COMMENT ON COLUMN "job"."company_id" IS '기업 아이디';

COMMENT ON COLUMN "job"."type" IS '공고 유형 (general, special)';

COMMENT ON COLUMN "job"."title" IS '제목';

COMMENT ON COLUMN "job"."location" IS '근무지';

COMMENT ON COLUMN "job"."employment_type" IS '고용 형태';

COMMENT ON COLUMN "job"."career" IS '경력';

COMMENT ON COLUMN "job"."description" IS '소개';

COMMENT ON COLUMN "job"."requirements" IS '자격 요건';

COMMENT ON COLUMN "job"."preferred" IS '우대 사항';

COMMENT ON COLUMN "job"."deadline" IS '마감일';

COMMENT ON COLUMN "job"."status" IS '상태 (draft, open, closed)';

COMMENT ON COLUMN "job"."created_at" IS '등록일';

COMMENT ON COLUMN "job"."updated_at" IS '수정일';

COMMENT ON COLUMN "job"."deleted_at" IS '삭제일';

CREATE TABLE "feedback_request_sections" (
	"section_id"	BIGINT		NOT NULL,
	"request_id"	BIGINT		NOT NULL,
	"section_type"	VARCHAR(30)		NOT NULL
);

COMMENT ON COLUMN "feedback_request_sections"."section_id" IS '섹션 아이디';

COMMENT ON COLUMN "feedback_request_sections"."request_id" IS '피드백 요청 아이디';

COMMENT ON COLUMN "feedback_request_sections"."section_type" IS '섹션 종류';

CREATE TABLE "company_offers" (
	"id"	BIGINT		NOT NULL,
	"company_id"	UUID		NOT NULL,
	"user_id"	UUID		NOT NULL,
	"job_id"	UUID		NULL,
	"message"	TEXT		NOT NULL,
	"status"	VARCHAR(20)		NOT NULL,
	"created_at"	TIMESTAMP		NOT NULL
);

COMMENT ON COLUMN "company_offers"."id" IS '아이디';

COMMENT ON COLUMN "company_offers"."company_id" IS '기업 아이디';

COMMENT ON COLUMN "company_offers"."user_id" IS '받는 사람 아이디';

COMMENT ON COLUMN "company_offers"."job_id" IS '공고 아이디';

COMMENT ON COLUMN "company_offers"."message" IS '제안 메시지';

COMMENT ON COLUMN "company_offers"."status" IS '제안 상태 (sent, accepted, declined)';

COMMENT ON COLUMN "company_offers"."created_at" IS '제안일';

CREATE TABLE "pinned_repos" (
	"id"	BIGINT		NOT NULL,
	"user_id"	UUID		NOT NULL,
	"repo_full_name"	VARCHAR(200)		NOT NULL,
	"order_no"	INT		NOT NULL,
	"created_at"	TIMESTAMP		NOT NULL
);

COMMENT ON COLUMN "pinned_repos"."id" IS '아이디';

COMMENT ON COLUMN "pinned_repos"."user_id" IS '유저 아이디';

COMMENT ON COLUMN "pinned_repos"."repo_full_name" IS '레포 이름';

COMMENT ON COLUMN "pinned_repos"."order_no" IS '표시 순서';

COMMENT ON COLUMN "pinned_repos"."created_at" IS '생성일';

CREATE TABLE "mentors" (
	"user_id"	UUID		NOT NULL,
	"company_name"	VARCHAR(100)		NOT NULL,
	"job_title"	VARCHAR(100)		NOT NULL,
	"price_credits"	INT		NOT NULL,
	"verified_at"	TIMESTAMP		NOT NULL
);

COMMENT ON COLUMN "mentors"."user_id" IS '유저 아이디';

COMMENT ON COLUMN "mentors"."company_name" IS '회사 이름';

COMMENT ON COLUMN "mentors"."job_title" IS '직무';

COMMENT ON COLUMN "mentors"."price_credits" IS '피드백 가격(크레딧)';

COMMENT ON COLUMN "mentors"."verified_at" IS '인증일';

CREATE TABLE "posts" (
	"id"	BIGINT		NOT NULL,
	"user_id"	UUID		NOT NULL,
	"type"	VARCHAR(20)		NOT NULL,
	"content"	TEXT		NULL,
	"image_url"	VARCHAR(500)		NULL,
	"video_url"	VARCHAR(500)		NULL,
	"blog_post_id"	BIGINT		NULL,
	"repo_full_name"	VARCHAR(200)		NULL,
	"created_at"	TIMESTAMP		NOT NULL,
	"updated_at"	TIMESTAMP		NULL,
	"deleted_at"	TIMESTAMP		NULL
);

COMMENT ON COLUMN "posts"."id" IS '아이디';

COMMENT ON COLUMN "posts"."user_id" IS '작성자 아이디';

COMMENT ON COLUMN "posts"."type" IS '글 유형 (general, tech, question, blog, repo)';

COMMENT ON COLUMN "posts"."content" IS '내용';

COMMENT ON COLUMN "posts"."image_url" IS '이미지 주소';

COMMENT ON COLUMN "posts"."video_url" IS '영상 주소';

COMMENT ON COLUMN "posts"."blog_post_id" IS '블로그 글 아이디';

COMMENT ON COLUMN "posts"."repo_full_name" IS '레포 이름';

COMMENT ON COLUMN "posts"."created_at" IS '작성일';

COMMENT ON COLUMN "posts"."updated_at" IS '수정일';

COMMENT ON COLUMN "posts"."deleted_at" IS '삭제일';

CREATE TABLE "paid_feedback_responses" (
	"id"	BIGINT		NOT NULL,
	"request_id"	BIGINT		NOT NULL,
	"content"	TEXT		NOT NULL,
	"created_at"	TIMESTAMP		NOT NULL
);

COMMENT ON COLUMN "paid_feedback_responses"."id" IS '아이디';

COMMENT ON COLUMN "paid_feedback_responses"."request_id" IS '유료 피드백 요청 아이디';

COMMENT ON COLUMN "paid_feedback_responses"."content" IS '답변 내용';

COMMENT ON COLUMN "paid_feedback_responses"."created_at" IS '답변일';

CREATE TABLE "feedback_comment_likes" (
	"comment_id"	BIGINT		NOT NULL,
	"user_id"	UUID		NOT NULL,
	"created_at"	TIMESTAMP		NOT NULL
);

COMMENT ON COLUMN "feedback_comment_likes"."comment_id" IS '피드백 댓글 아이디';

COMMENT ON COLUMN "feedback_comment_likes"."user_id" IS '유저 아이디';

COMMENT ON COLUMN "feedback_comment_likes"."created_at" IS '좋아요 시각';

CREATE TABLE "company_subscriptions" (
	"company_id"	UUID		NOT NULL,
	"plan"	VARCHAR(20)		NOT NULL,
	"trial_ends_at"	TIMESTAMP		NULL,
	"expires_at"	TIMESTAMP		NULL,
	"updated_at"	TIMESTAMP		NOT NULL
);

COMMENT ON COLUMN "company_subscriptions"."company_id" IS '기업 아이디';

COMMENT ON COLUMN "company_subscriptions"."plan" IS '요금제 (free, pro)';

COMMENT ON COLUMN "company_subscriptions"."trial_ends_at" IS '체험 종료일';

COMMENT ON COLUMN "company_subscriptions"."expires_at" IS '만료일';

COMMENT ON COLUMN "company_subscriptions"."updated_at" IS '수정일';

CREATE TABLE "users" (
	"id"	UUID		NOT NULL,
	"username"	VARCHAR(50)		NOT NULL,
	"email"	VARCHAR(255)		NULL,
	"password"	VARCHAR(255)		NULL,
	"role"	VARCHAR(20)		NOT NULL,
	"nickname"	VARCHAR(50)		NULL,
	"profile_image_url"	VARCHAR(500)		NULL,
	"self_introduction"	TEXT		NULL,
	"widgets"	JSONB		NULL,
	"pinned_common_resume_id"	BIGINT		NULL,
	"displayed_badge_id"	INT		NULL,
	"subscription"	BOOLEAN		NULL,
	"created_at"	TIMESTAMP		NOT NULL,
	"updated_at"	TIMESTAMP		NULL
);

COMMENT ON COLUMN "users"."id" IS '아이디';

COMMENT ON COLUMN "users"."username" IS '유저네임';

COMMENT ON COLUMN "users"."email" IS '이메일';

COMMENT ON COLUMN "users"."password" IS '패스워드';

COMMENT ON COLUMN "users"."role" IS '권한 (USER, COMPANY, ADMIN)';

COMMENT ON COLUMN "users"."nickname" IS '닉네임';

COMMENT ON COLUMN "users"."profile_image_url" IS '프로필 사진';

COMMENT ON COLUMN "users"."self_introduction" IS '자기소개';

COMMENT ON COLUMN "users"."widgets" IS '위젯 설정';

COMMENT ON COLUMN "users"."pinned_common_resume_id" IS '대표 이력서 아이디';

COMMENT ON COLUMN "users"."displayed_badge_id" IS '표시할 배지 아이디';

COMMENT ON COLUMN "users"."subscription" IS '유료 구독';

COMMENT ON COLUMN "users"."created_at" IS '생성일';

COMMENT ON COLUMN "users"."updated_at" IS '수정일';

CREATE TABLE "paid_feedback_requests" (
	"id"	BIGINT		NOT NULL,
	"requester_id"	UUID		NOT NULL,
	"mentor_id"	UUID		NOT NULL,
	"common_resume_id"	BIGINT		NULL,
	"target_resume_id"	BIGINT		NULL,
	"content"	TEXT		NULL,
	"credits"	INT		NOT NULL,
	"status"	VARCHAR(20)		NOT NULL,
	"created_at"	TIMESTAMP		NOT NULL,
	"updated_at"	TIMESTAMP		NULL
);

COMMENT ON COLUMN "paid_feedback_requests"."id" IS '아이디';

COMMENT ON COLUMN "paid_feedback_requests"."requester_id" IS '요청자 아이디';

COMMENT ON COLUMN "paid_feedback_requests"."mentor_id" IS '현직자 아이디';

COMMENT ON COLUMN "paid_feedback_requests"."common_resume_id" IS '공통 이력서 아이디';

COMMENT ON COLUMN "paid_feedback_requests"."target_resume_id" IS '특화 이력서 아이디';

COMMENT ON COLUMN "paid_feedback_requests"."content" IS '요청 내용';

COMMENT ON COLUMN "paid_feedback_requests"."credits" IS '결제 크레딧';

COMMENT ON COLUMN "paid_feedback_requests"."status" IS '진행 상태 (requested, in_progress, completed, canceled)';

COMMENT ON COLUMN "paid_feedback_requests"."created_at" IS '요청일';

COMMENT ON COLUMN "paid_feedback_requests"."updated_at" IS '수정일';

CREATE TABLE "job_questions" (
	"id"	BIGINT		NOT NULL,
	"job_id"	UUID		NOT NULL,
	"content"	VARCHAR(300)		NOT NULL,
	"char_limit"	INT		NULL,
	"order_no"	INT		NOT NULL
);

COMMENT ON COLUMN "job_questions"."id" IS '아이디';

COMMENT ON COLUMN "job_questions"."job_id" IS '공고 아이디';

COMMENT ON COLUMN "job_questions"."content" IS '문항 내용';

COMMENT ON COLUMN "job_questions"."char_limit" IS '글자 수 제한';

COMMENT ON COLUMN "job_questions"."order_no" IS '문항 순서';

CREATE TABLE "feedback_comments" (
	"comment_id"	BIGINT		NOT NULL,
	"feedback_id"	BIGINT		NOT NULL,
	"parent_id"	BIGINT		NULL,
	"user_id"	UUID		NOT NULL,
	"section"	VARCHAR(30)		NULL,
	"content"	TEXT		NOT NULL,
	"is_adopted"	BOOLEAN	DEFAULT false	NOT NULL,
	"adopted_at"	TIMESTAMP		NULL,
	"created_at"	TIMESTAMP		NOT NULL,
	"updated_at"	TIMESTAMP		NULL
);

COMMENT ON COLUMN "feedback_comments"."comment_id" IS '댓글 아이디';

COMMENT ON COLUMN "feedback_comments"."feedback_id" IS '피드백 요청 아이디';

COMMENT ON COLUMN "feedback_comments"."parent_id" IS '부모 댓글 아이디';

COMMENT ON COLUMN "feedback_comments"."user_id" IS '작성자 아이디';

COMMENT ON COLUMN "feedback_comments"."section" IS '피드백 분야';

COMMENT ON COLUMN "feedback_comments"."content" IS '내용';

COMMENT ON COLUMN "feedback_comments"."is_adopted" IS '채택 여부';

COMMENT ON COLUMN "feedback_comments"."adopted_at" IS '채택일';

COMMENT ON COLUMN "feedback_comments"."created_at" IS '작성일';

COMMENT ON COLUMN "feedback_comments"."updated_at" IS '수정일';

CREATE TABLE "SocialAccount" (
	"id"	Long		NOT NULL,
	"user_id"	UUID		NOT NULL,
	"provider"	VARCHAR(20)		NOT NULL,
	"subject"	VARCHAR(255)		NOT NULL
);

COMMENT ON COLUMN "SocialAccount"."user_id" IS '아이디';

CREATE TABLE "target_resumes" (
	"id"	BIGINT		NOT NULL,
	"job_id"	UUID		NOT NULL,
	"user_id"	UUID		NOT NULL,
	"common_resume_id"	BIGINT		NULL,
	"title"	VARCHAR(100)		NOT NULL,
	"content"	JSONB		NOT NULL,
	"created_at"	TIMESTAMP		NOT NULL,
	"updated_at"	TIMESTAMP		NULL
);

COMMENT ON COLUMN "target_resumes"."id" IS '아이디';

COMMENT ON COLUMN "target_resumes"."job_id" IS '공고 아이디';

COMMENT ON COLUMN "target_resumes"."user_id" IS '유저 아이디';

COMMENT ON COLUMN "target_resumes"."common_resume_id" IS '공통 이력서 아이디';

COMMENT ON COLUMN "target_resumes"."title" IS '제목';

COMMENT ON COLUMN "target_resumes"."content" IS '이력서 내용';

COMMENT ON COLUMN "target_resumes"."created_at" IS '작성일';

COMMENT ON COLUMN "target_resumes"."updated_at" IS '수정일';

CREATE TABLE "feedback_requests" (
	"id"	BIGINT		NOT NULL,
	"user_id"	UUID		NOT NULL,
	"common_resume_id"	BIGINT		NULL,
	"target_resume_id"	BIGINT		NULL,
	"content"	TEXT		NULL,
	"category"	VARCHAR(20)		NULL,
	"created_at"	TIMESTAMP		NOT NULL,
	"updated_at"	TIMESTAMP		NULL,
	"deleted_at"	TIMESTAMP		NULL
);

COMMENT ON COLUMN "feedback_requests"."id" IS '아이디';

COMMENT ON COLUMN "feedback_requests"."user_id" IS '요청자 아이디';

COMMENT ON COLUMN "feedback_requests"."common_resume_id" IS '공통 이력서 아이디';

COMMENT ON COLUMN "feedback_requests"."target_resume_id" IS '특화 이력서 아이디';

COMMENT ON COLUMN "feedback_requests"."content" IS '요청 메시지';

COMMENT ON COLUMN "feedback_requests"."category" IS '분야 (frontend, backend)';

COMMENT ON COLUMN "feedback_requests"."created_at" IS '요청일';

COMMENT ON COLUMN "feedback_requests"."updated_at" IS '수정일';

COMMENT ON COLUMN "feedback_requests"."deleted_at" IS '취소일';

CREATE TABLE "best_reviews" (
	"id"	BIGINT		NOT NULL,
	"comment_id"	BIGINT		NOT NULL,
	"period"	VARCHAR(20)		NOT NULL,
	"period_start"	DATE		NOT NULL,
	"rank_no"	INT		NOT NULL,
	"created_at"	TIMESTAMP		NOT NULL
);

COMMENT ON COLUMN "best_reviews"."id" IS '아이디';

COMMENT ON COLUMN "best_reviews"."comment_id" IS '피드백 댓글 아이디';

COMMENT ON COLUMN "best_reviews"."period" IS '선정 기간 (weekly, monthly)';

COMMENT ON COLUMN "best_reviews"."period_start" IS '기간 시작일';

COMMENT ON COLUMN "best_reviews"."rank_no" IS '순위';

COMMENT ON COLUMN "best_reviews"."created_at" IS '선정일';

ALTER TABLE "blog_posts" ADD CONSTRAINT "PK_BLOG_POSTS" PRIMARY KEY (
	"id"
);

ALTER TABLE "job_applications" ADD CONSTRAINT "PK_JOB_APPLICATIONS" PRIMARY KEY (
	"id"
);

ALTER TABLE "badge" ADD CONSTRAINT "PK_BADGE" PRIMARY KEY (
	"id"
);

ALTER TABLE "comments" ADD CONSTRAINT "PK_COMMENTS" PRIMARY KEY (
	"id"
);

ALTER TABLE "user_badge" ADD CONSTRAINT "PK_USER_BADGE" PRIMARY KEY (
	"user_id",
	"badge_id"
);

ALTER TABLE "follows" ADD CONSTRAINT "PK_FOLLOWS" PRIMARY KEY (
	"follower_id",
	"following_id"
);

ALTER TABLE "verifications" ADD CONSTRAINT "PK_VERIFICATIONS" PRIMARY KEY (
	"id"
);

ALTER TABLE "credit_payments" ADD CONSTRAINT "PK_CREDIT_PAYMENTS" PRIMARY KEY (
	"id"
);

ALTER TABLE "integrations" ADD CONSTRAINT "PK_INTEGRATIONS" PRIMARY KEY (
	"id"
);

ALTER TABLE "credit_transactions" ADD CONSTRAINT "PK_CREDIT_TRANSACTIONS" PRIMARY KEY (
	"id"
);

ALTER TABLE "post_like" ADD CONSTRAINT "PK_POST_LIKE" PRIMARY KEY (
	"post_id",
	"user_id"
);

ALTER TABLE "common_resumes" ADD CONSTRAINT "PK_COMMON_RESUMES" PRIMARY KEY (
	"id"
);

ALTER TABLE "job" ADD CONSTRAINT "PK_JOB" PRIMARY KEY (
	"id"
);

ALTER TABLE "feedback_request_sections" ADD CONSTRAINT "PK_FEEDBACK_REQUEST_SECTIONS" PRIMARY KEY (
	"section_id"
);

ALTER TABLE "company_offers" ADD CONSTRAINT "PK_COMPANY_OFFERS" PRIMARY KEY (
	"id"
);

ALTER TABLE "pinned_repos" ADD CONSTRAINT "PK_PINNED_REPOS" PRIMARY KEY (
	"id"
);

ALTER TABLE "mentors" ADD CONSTRAINT "PK_MENTORS" PRIMARY KEY (
	"user_id"
);

ALTER TABLE "posts" ADD CONSTRAINT "PK_POSTS" PRIMARY KEY (
	"id"
);

ALTER TABLE "paid_feedback_responses" ADD CONSTRAINT "PK_PAID_FEEDBACK_RESPONSES" PRIMARY KEY (
	"id"
);

ALTER TABLE "feedback_comment_likes" ADD CONSTRAINT "PK_FEEDBACK_COMMENT_LIKES" PRIMARY KEY (
	"comment_id",
	"user_id"
);

ALTER TABLE "company_subscriptions" ADD CONSTRAINT "PK_COMPANY_SUBSCRIPTIONS" PRIMARY KEY (
	"company_id"
);

ALTER TABLE "users" ADD CONSTRAINT "PK_USERS" PRIMARY KEY (
	"id"
);

ALTER TABLE "paid_feedback_requests" ADD CONSTRAINT "PK_PAID_FEEDBACK_REQUESTS" PRIMARY KEY (
	"id"
);

ALTER TABLE "job_questions" ADD CONSTRAINT "PK_JOB_QUESTIONS" PRIMARY KEY (
	"id"
);

ALTER TABLE "feedback_comments" ADD CONSTRAINT "PK_FEEDBACK_COMMENTS" PRIMARY KEY (
	"comment_id"
);

ALTER TABLE "SocialAccount" ADD CONSTRAINT "PK_SOCIALACCOUNT" PRIMARY KEY (
	"id",
	"user_id"
);

ALTER TABLE "target_resumes" ADD CONSTRAINT "PK_TARGET_RESUMES" PRIMARY KEY (
	"id"
);

ALTER TABLE "feedback_requests" ADD CONSTRAINT "PK_FEEDBACK_REQUESTS" PRIMARY KEY (
	"id"
);

ALTER TABLE "best_reviews" ADD CONSTRAINT "PK_BEST_REVIEWS" PRIMARY KEY (
	"id"
);

ALTER TABLE "blog_posts" ADD CONSTRAINT "FK_users_TO_blog_posts_1" FOREIGN KEY (
	"user_id"
)
REFERENCES "users" (
	"id"
);

ALTER TABLE "blog_posts" ADD CONSTRAINT "FK_integrations_TO_blog_posts_1" FOREIGN KEY (
	"integration_id"
)
REFERENCES "integrations" (
	"id"
);

ALTER TABLE "job_applications" ADD CONSTRAINT "FK_job_TO_job_applications_1" FOREIGN KEY (
	"job_id"
)
REFERENCES "job" (
	"id"
);

ALTER TABLE "job_applications" ADD CONSTRAINT "FK_users_TO_job_applications_1" FOREIGN KEY (
	"user_id"
)
REFERENCES "users" (
	"id"
);

ALTER TABLE "job_applications" ADD CONSTRAINT "FK_common_resumes_TO_job_applications_1" FOREIGN KEY (
	"common_resume_id"
)
REFERENCES "common_resumes" (
	"id"
);

ALTER TABLE "job_applications" ADD CONSTRAINT "FK_target_resumes_TO_job_applications_1" FOREIGN KEY (
	"target_resume_id"
)
REFERENCES "target_resumes" (
	"id"
);

ALTER TABLE "comments" ADD CONSTRAINT "FK_posts_TO_comments_1" FOREIGN KEY (
	"post_id"
)
REFERENCES "posts" (
	"id"
);

ALTER TABLE "comments" ADD CONSTRAINT "FK_users_TO_comments_1" FOREIGN KEY (
	"user_id"
)
REFERENCES "users" (
	"id"
);

ALTER TABLE "comments" ADD CONSTRAINT "FK_comments_TO_comments_1" FOREIGN KEY (
	"parent_id"
)
REFERENCES "comments" (
	"id"
);

ALTER TABLE "user_badge" ADD CONSTRAINT "FK_users_TO_user_badge_1" FOREIGN KEY (
	"user_id"
)
REFERENCES "users" (
	"id"
);

ALTER TABLE "user_badge" ADD CONSTRAINT "FK_badge_TO_user_badge_1" FOREIGN KEY (
	"badge_id"
)
REFERENCES "badge" (
	"id"
);

ALTER TABLE "follows" ADD CONSTRAINT "FK_users_TO_follows_1" FOREIGN KEY (
	"follower_id"
)
REFERENCES "users" (
	"id"
);

ALTER TABLE "follows" ADD CONSTRAINT "FK_users_TO_follows_2" FOREIGN KEY (
	"following_id"
)
REFERENCES "users" (
	"id"
);

ALTER TABLE "verifications" ADD CONSTRAINT "FK_users_TO_verifications_1" FOREIGN KEY (
	"user_id"
)
REFERENCES "users" (
	"id"
);

ALTER TABLE "credit_payments" ADD CONSTRAINT "FK_users_TO_credit_payments_1" FOREIGN KEY (
	"user_id"
)
REFERENCES "users" (
	"id"
);

ALTER TABLE "integrations" ADD CONSTRAINT "FK_users_TO_integrations_1" FOREIGN KEY (
	"user_id"
)
REFERENCES "users" (
	"id"
);

ALTER TABLE "credit_transactions" ADD CONSTRAINT "FK_users_TO_credit_transactions_1" FOREIGN KEY (
	"user_id"
)
REFERENCES "users" (
	"id"
);

ALTER TABLE "credit_transactions" ADD CONSTRAINT "FK_paid_feedback_requests_TO_credit_transactions_1" FOREIGN KEY (
	"paid_feedback_request_id"
)
REFERENCES "paid_feedback_requests" (
	"id"
);

ALTER TABLE "credit_transactions" ADD CONSTRAINT "FK_credit_payments_TO_credit_transactions_1" FOREIGN KEY (
	"credit_payment_id"
)
REFERENCES "credit_payments" (
	"id"
);

ALTER TABLE "post_like" ADD CONSTRAINT "FK_posts_TO_post_like_1" FOREIGN KEY (
	"post_id"
)
REFERENCES "posts" (
	"id"
);

ALTER TABLE "post_like" ADD CONSTRAINT "FK_users_TO_post_like_1" FOREIGN KEY (
	"user_id"
)
REFERENCES "users" (
	"id"
);

ALTER TABLE "common_resumes" ADD CONSTRAINT "FK_users_TO_common_resumes_1" FOREIGN KEY (
	"user_id"
)
REFERENCES "users" (
	"id"
);

ALTER TABLE "job" ADD CONSTRAINT "FK_users_TO_job_1" FOREIGN KEY (
	"company_id"
)
REFERENCES "users" (
	"id"
);

ALTER TABLE "feedback_request_sections" ADD CONSTRAINT "FK_feedback_requests_TO_feedback_request_sections_1" FOREIGN KEY (
	"request_id"
)
REFERENCES "feedback_requests" (
	"id"
);

ALTER TABLE "company_offers" ADD CONSTRAINT "FK_users_TO_company_offers_1" FOREIGN KEY (
	"company_id"
)
REFERENCES "users" (
	"id"
);

ALTER TABLE "company_offers" ADD CONSTRAINT "FK_users_TO_company_offers_2" FOREIGN KEY (
	"user_id"
)
REFERENCES "users" (
	"id"
);

ALTER TABLE "company_offers" ADD CONSTRAINT "FK_job_TO_company_offers_1" FOREIGN KEY (
	"job_id"
)
REFERENCES "job" (
	"id"
);

ALTER TABLE "pinned_repos" ADD CONSTRAINT "FK_users_TO_pinned_repos_1" FOREIGN KEY (
	"user_id"
)
REFERENCES "users" (
	"id"
);

ALTER TABLE "mentors" ADD CONSTRAINT "FK_users_TO_mentors_1" FOREIGN KEY (
	"user_id"
)
REFERENCES "users" (
	"id"
);

ALTER TABLE "posts" ADD CONSTRAINT "FK_users_TO_posts_1" FOREIGN KEY (
	"user_id"
)
REFERENCES "users" (
	"id"
);

ALTER TABLE "posts" ADD CONSTRAINT "FK_blog_posts_TO_posts_1" FOREIGN KEY (
	"blog_post_id"
)
REFERENCES "blog_posts" (
	"id"
);

ALTER TABLE "paid_feedback_responses" ADD CONSTRAINT "FK_paid_feedback_requests_TO_paid_feedback_responses_1" FOREIGN KEY (
	"request_id"
)
REFERENCES "paid_feedback_requests" (
	"id"
);

ALTER TABLE "feedback_comment_likes" ADD CONSTRAINT "FK_feedback_comments_TO_feedback_comment_likes_1" FOREIGN KEY (
	"comment_id"
)
REFERENCES "feedback_comments" (
	"comment_id"
);

ALTER TABLE "feedback_comment_likes" ADD CONSTRAINT "FK_users_TO_feedback_comment_likes_1" FOREIGN KEY (
	"user_id"
)
REFERENCES "users" (
	"id"
);

ALTER TABLE "company_subscriptions" ADD CONSTRAINT "FK_users_TO_company_subscriptions_1" FOREIGN KEY (
	"company_id"
)
REFERENCES "users" (
	"id"
);

ALTER TABLE "users" ADD CONSTRAINT "FK_common_resumes_TO_users_1" FOREIGN KEY (
	"pinned_common_resume_id"
)
REFERENCES "common_resumes" (
	"id"
);

ALTER TABLE "users" ADD CONSTRAINT "FK_badge_TO_users_1" FOREIGN KEY (
	"displayed_badge_id"
)
REFERENCES "badge" (
	"id"
);

ALTER TABLE "paid_feedback_requests" ADD CONSTRAINT "FK_users_TO_paid_feedback_requests_1" FOREIGN KEY (
	"requester_id"
)
REFERENCES "users" (
	"id"
);

ALTER TABLE "paid_feedback_requests" ADD CONSTRAINT "FK_mentors_TO_paid_feedback_requests_1" FOREIGN KEY (
	"mentor_id"
)
REFERENCES "mentors" (
	"user_id"
);

ALTER TABLE "paid_feedback_requests" ADD CONSTRAINT "FK_common_resumes_TO_paid_feedback_requests_1" FOREIGN KEY (
	"common_resume_id"
)
REFERENCES "common_resumes" (
	"id"
);

ALTER TABLE "paid_feedback_requests" ADD CONSTRAINT "FK_target_resumes_TO_paid_feedback_requests_1" FOREIGN KEY (
	"target_resume_id"
)
REFERENCES "target_resumes" (
	"id"
);

ALTER TABLE "job_questions" ADD CONSTRAINT "FK_job_TO_job_questions_1" FOREIGN KEY (
	"job_id"
)
REFERENCES "job" (
	"id"
);

ALTER TABLE "feedback_comments" ADD CONSTRAINT "FK_feedback_requests_TO_feedback_comments_1" FOREIGN KEY (
	"feedback_id"
)
REFERENCES "feedback_requests" (
	"id"
);

ALTER TABLE "feedback_comments" ADD CONSTRAINT "FK_feedback_comments_TO_feedback_comments_1" FOREIGN KEY (
	"parent_id"
)
REFERENCES "feedback_comments" (
	"comment_id"
);

ALTER TABLE "feedback_comments" ADD CONSTRAINT "FK_users_TO_feedback_comments_1" FOREIGN KEY (
	"user_id"
)
REFERENCES "users" (
	"id"
);

ALTER TABLE "SocialAccount" ADD CONSTRAINT "FK_users_TO_SocialAccount_1" FOREIGN KEY (
	"user_id"
)
REFERENCES "users" (
	"id"
);

ALTER TABLE "target_resumes" ADD CONSTRAINT "FK_job_TO_target_resumes_1" FOREIGN KEY (
	"job_id"
)
REFERENCES "job" (
	"id"
);

ALTER TABLE "target_resumes" ADD CONSTRAINT "FK_users_TO_target_resumes_1" FOREIGN KEY (
	"user_id"
)
REFERENCES "users" (
	"id"
);

ALTER TABLE "target_resumes" ADD CONSTRAINT "FK_common_resumes_TO_target_resumes_1" FOREIGN KEY (
	"common_resume_id"
)
REFERENCES "common_resumes" (
	"id"
);

ALTER TABLE "feedback_requests" ADD CONSTRAINT "FK_users_TO_feedback_requests_1" FOREIGN KEY (
	"user_id"
)
REFERENCES "users" (
	"id"
);

ALTER TABLE "feedback_requests" ADD CONSTRAINT "FK_common_resumes_TO_feedback_requests_1" FOREIGN KEY (
	"common_resume_id"
)
REFERENCES "common_resumes" (
	"id"
);

ALTER TABLE "feedback_requests" ADD CONSTRAINT "FK_target_resumes_TO_feedback_requests_1" FOREIGN KEY (
	"target_resume_id"
)
REFERENCES "target_resumes" (
	"id"
);

ALTER TABLE "best_reviews" ADD CONSTRAINT "FK_feedback_comments_TO_best_reviews_1" FOREIGN KEY (
	"comment_id"
)
REFERENCES "feedback_comments" (
	"comment_id"
);

