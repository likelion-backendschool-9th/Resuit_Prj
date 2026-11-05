package com.newlecture.resuit.service;

import com.newlecture.resuit.dto.feedback.FeedbackRequestSearchCondition;
import com.newlecture.resuit.dto.feedback.FeedbackRequestSummary;
import java.util.List;

// 피드백 게시판(4.x). 로그인·잘못된 값 오류는 CommonResumeService의 예외를 함께 쓴다.
public interface FeedbackBoardService {
    List<FeedbackRequestSummary> getRequests(String username, FeedbackRequestSearchCondition condition);
}
