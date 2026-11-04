package com.newlecture.resuit.service;

import com.newlecture.resuit.dto.feedback.FeedbackRequestCreateRequest;
import com.newlecture.resuit.dto.feedback.FeedbackRequestResponse;

// 로그인·이력서 없음·권한·잘못된 값 오류는 CommonResumeService의 예외를 함께 쓴다.
public interface FeedbackRequestService {
    FeedbackRequestResponse create(String username, FeedbackRequestCreateRequest request);

    public static class FeedbackRequestNotFoundException extends RuntimeException {}
    public static class FeedbackRequestForbiddenException extends RuntimeException {}
    public static class FeedbackRequestExistsException extends RuntimeException {}
}
