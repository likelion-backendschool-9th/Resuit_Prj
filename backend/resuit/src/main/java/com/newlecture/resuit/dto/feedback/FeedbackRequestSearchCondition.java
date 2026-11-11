package com.newlecture.resuit.dto.feedback;

// 피드백 게시판 목록 조건(4.1, 4.3, 4.4). category: FRONTEND, BACKEND. 없으면 전체
public record FeedbackRequestSearchCondition(
    String category,
    boolean mine
) {}
