package com.newlecture.resuit.service;

// 알림 종류. notifications.type에 이 이름이 저장된다.
public enum NotificationType {
	// 내 게시글에 댓글이 달림
	POST_COMMENT,
	// 내 댓글에 답글이 달림
	COMMENT_REPLY,
	// 내 피드백 요청에 피드백이 달림
	FEEDBACK_COMMENT,
	// 내 피드백이 채택됨
	FEEDBACK_ADOPTED,
	// 현직자가 유료 피드백 요청을 받음
	PAID_FEEDBACK_REQUESTED,
	// 요청자가 유료 피드백 답변을 받음
	PAID_FEEDBACK_ANSWERED,
	// 구직자가 채용 제안을 받음
	OFFER_RECEIVED,
	// 기업이 보낸 제안에 승낙·거절 응답을 받음
	OFFER_RESPONDED,
	// 기업이 공고 지원을 받음
	JOB_APPLIED
}
