# 리뷰어 배지 갱신 규칙

- 매년 1월·4월·7월·10월 1일 00:05(한국 시간)에 직전 두 분기 활동을 평가한다. 평가 범위의 끝은 분기 시작일 00:00이고, 시작은 그보다 6개월 전이다.
- 리뷰어: 채택된 피드백 5건 이상, 서로 다른 요청자 3명 이상.
- 우수 리뷰어: 채택 15건 이상, 서로 다른 요청자 8명 이상, 같은 평가 범위에서 베스트 리뷰 선정 1회 이상.
- 현재 활성 배지가 미달이면 `GRACE`로 바꾸고 다음 분기 시작일을 `grace_until`에 저장한다. `GRACE`는 프로필에 표시하지만 기업의 활성 배지 필터와 혜택에는 포함하지 않는다.
- 다음 분기 평가에서도 미달이면 `EXPIRED`가 된다. 지난 활성 기간은 `user_badge_history`에 남긴다. 유예 중 자격을 다시 채우면 `ACTIVE`로 돌아갈 수 있다.
- `badge.validity_days`는 이 분기형 리뷰어 배지에 사용하지 않는다. `user_badge.expires_at`은 실제 만료 시각으로 사용하고, 유예 기한은 `grace_until`로 분리한다.
- `ReviewController`와 `ResumeController`의 채택 API는 `FeedbackAdoptionService`를 호출한다. 요청자 권한과 기존 채택 여부를 확인한 뒤 채택을 저장하고 `ReviewerBadgeService.refreshAfterActivity`로 즉시 재평가한다.
- 배지 상태는 `status`가 기준이다. 기존 `is_eligible`은 `ACTIVE`일 때만 참으로 동기화한다. 기업 필터는 `status = 'ACTIVE'` 조건으로 조회한다.
- 기존 `user_badge` 행은 [마이그레이션](./migrations/2026-11-04-reviewer-badge-renewal.sql)에서 활성 또는 만료로 이전한다. 이전 데이터에 유예 여부나 여러 인증 기간의 이력이 없으므로 과거 이력은 복원할 수 없다.
- 분기 작업은 현재 단일 서버 실행을 전제로 한다. 서버를 여러 대 띄우면 분산 작업 잠금을 적용해야 한다.
