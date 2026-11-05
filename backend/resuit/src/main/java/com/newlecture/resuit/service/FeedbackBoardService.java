package com.newlecture.resuit.service;

import com.newlecture.resuit.dto.feedback.FeedbackRequestSearchCondition;
import com.newlecture.resuit.dto.feedback.FeedbackRequestSummary;
import com.newlecture.resuit.dto.resume.ResumePreviewCondition;
import com.newlecture.resuit.dto.resume.ResumePreviewResponse;
import java.util.List;

// 피드백 게시판(4.x). 로그인·잘못된 값 오류는 CommonResumeService의 예외를 함께 쓴다.
public interface FeedbackBoardService {
    List<FeedbackRequestSummary> getRequests(String username, FeedbackRequestSearchCondition condition);
    ResumePreviewResponse getResumePreview(String username, ResumePreviewCondition condition);
}
